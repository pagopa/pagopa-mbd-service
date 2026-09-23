data "azurerm_key_vault" "domain_key_vault" {
  name                = "pagopa-${var.env_short}-${local.location_short}-${local.domain}-kv"
  resource_group_name = "pagopa-${var.env_short}-${local.location_short}-${local.domain}-sec-rg"
}

data "azurerm_key_vault_secret" "payments_key_subscription_key" {
  name         = "apikey-gpd-payments"
  key_vault_id = data.azurerm_key_vault.domain_key_vault.id
}

data "azurerm_api_management_api" "apim_api_authorizer_config_api_v1" {
  name                = "pagopa-${var.env_short}-weu-shared-authorizer-config-api-v1"
  api_management_name = local.apim.name
  resource_group_name = local.apim.rg
  revision            = "1"
}

resource "azurerm_api_management_subscription" "authorizer_config_subkey" {
  api_management_name = local.apim.name
  resource_group_name = local.apim.rg

  api_id        = replace(data.azurerm_api_management_api.apim_api_authorizer_config_api_v1.id, ";rev=1", "")
  display_name  = "Authorizer Config for eBollo autoconfiguration"
  allow_tracing = false
  state         = "active"
}

# -----------------------------------------------------------------------------
# create_authorizer_config
# -----------------------------------------------------------------------------
# Ensures the authorizer DB contains a configuration for the gpd-payments
# subkey. Runs on every `terraform apply` and never on `terraform plan`.
#
# Behavior:
#   - POST /authorizations with the current subkey read from Key Vault.
#   - HTTP 200 (created) or 409 (already present) => success (idempotent).
#   - Any other status => apply fails, response body printed to logs.
#
# Side effects / things to be aware of:
#   - `triggers.always_run = timestamp()` forces the resource to be considered
#     changed on every plan. Expect `terraform plan` to always show this
#     resource as needing to be replaced. This is intentional and cosmetic;
#     the actual POST only fires during `apply` (provisioners never run on
#     plan).
#   - The POST is executed against the real authorizer API on every apply,
#     including from local developer machines. The API is idempotent so this
#     is safe, but it does produce one API call and one audit-log entry per
#     apply.
#   - This resource does NOT clean up stale rows on subkey rotation; that is
#     handled by `cleanup_authorizer_config` below.
# -----------------------------------------------------------------------------
resource "null_resource" "create_authorizer_config" {
  triggers = {
    always_run = timestamp()
  }

  provisioner "local-exec" {
    interpreter = ["bash", "-c"]

    environment = {
      APIM_HOSTNAME             = local.apim.hostname
      AUTHORIZER_SERVICE_SUBKEY = azurerm_api_management_subscription.authorizer_config_subkey.primary_key
      GPD_PAYMENTS_SUB_KEY      = data.azurerm_key_vault_secret.payments_key_subscription_key.value
    }

    command = <<-EOT
      set -euo pipefail

      body=$(jq -n --arg subkey "$GPD_PAYMENTS_SUB_KEY" '{
        domain: "gpd",
        subscription_key: $subkey,
        description: "Key configuration for eBollo mbd-service",
        owner: { id: "15376371009", name: "PagoPa S.p.A", type: "BROKER" },
        authorized_entities: [ { name: "All entities", value: "*" } ],
        other_metadata: []
      }')

      resp_file=$(mktemp)
      status=$(curl -sS -o "$resp_file" -w "%%{http_code}" \
        -X POST "https://$APIM_HOSTNAME/shared/authorizer-config/v1/authorizations" \
        -H "Content-Type: application/json" \
        -H "Ocp-Apim-Subscription-Key: $AUTHORIZER_SERVICE_SUBKEY" \
        --data "$body")

      if [ "$status" = "200" ] || [ "$status" = "409" ]; then
        echo "Authorizer config check OK (HTTP $status)"
        rm -f "$resp_file"
        exit 0
      fi

      echo "Authorizer config call failed: HTTP $status"
      cat "$resp_file"
      rm -f "$resp_file"
      exit 1
    EOT
  }
}

# -----------------------------------------------------------------------------
# cleanup_authorizer_config
# -----------------------------------------------------------------------------
# Prevents stale rows from accumulating in the authorizer DB when the
# gpd-payments subkey rotates. Only meaningful action happens on destroy of a
# previous instance of this resource (i.e., on rotation).
#
# Behavior:
#   - `triggers` captures the current subkey, APIM hostname, and authorizer
#     service subkey. When ANY value changes, Terraform replaces the resource:
#     destroys the OLD instance (running the destroy provisioner with the OLD
#     trigger values via `self.triggers`) and creates a new one.
#   - Destroy provisioner:
#       1. GET /authorizations/subkey/{oldSubkey} to obtain the id.
#       2. DELETE /authorizations/{id}.
#     Accepted statuses: 200/204 (deleted), 404 (already gone), empty
#     response (nothing to do). Anything else fails the apply.
#   - Create is a no-op; the row for the NEW subkey is (re)created by
#     `create_authorizer_config` on the same apply.
#
# Side effects / things to be aware of:
#   - Terraform forbids referencing anything except `self`, `count`, or `each`
#     inside a destroy provisioner. That's why hostname and authorizer service
#     subkey are duplicated into `triggers` — they must be readable via
#     `self.triggers` on the resource being destroyed.
#   - Rotating the authorizer service subkey (i.e., the APIM subscription
#     `authorizer_config_subkey`, e.g., from the Azure portal) ALSO triggers
#     replacement. The destroy provisioner then DELETEs the current authorizer
#     row using the OLD authorizer service subkey stored in triggers, and
#     `create_authorizer_config` re-POSTs it on the same apply. Functionally
#     correct but produces an extra DELETE + re-POST cycle on the authorizer
#     DB. As long as `terraform apply` is run BEFORE the old key is revoked,
#     this works.
#   - CAVEAT: if the authorizer service subkey is rotated in the portal
#     WITHOUT running `terraform apply` before the old key is invalidated,
#     the destroy provisioner will 401 (state still holds the invalid old
#     key). Recover with
#     `terraform state rm null_resource.cleanup_authorizer_config`
#     to skip the failed destroy, then clean the stale row manually via API.
#   - The hostname trigger never changes in practice (fixed per environment).
#   - On `terraform destroy` of this stack, the destroy provisioner fires and
#     removes the authorizer row. This is the correct teardown behavior but
#     means destroying the stack revokes access for mbd-service.
# -----------------------------------------------------------------------------
resource "null_resource" "cleanup_authorizer_config" {
  triggers = {
    subkey                    = data.azurerm_key_vault_secret.payments_key_subscription_key.value
    apim_hostname             = local.apim.hostname
    authorizer_service_subkey = azurerm_api_management_subscription.authorizer_config_subkey.primary_key
  }

  provisioner "local-exec" {
    when        = destroy
    interpreter = ["bash", "-c"]

    environment = {
      APIM_HOSTNAME             = self.triggers.apim_hostname
      AUTHORIZER_SERVICE_SUBKEY = self.triggers.authorizer_service_subkey
      GPD_PAYMENTS_SUB_KEY      = self.triggers.subkey
    }

    command = <<-EOT
      set -euo pipefail

      base_url="https://$APIM_HOSTNAME/shared/authorizer-config/v1/authorizations"

      # 1) Lookup the authorization by subkey to obtain its id.
      lookup_file=$(mktemp)
      lookup_status=$(curl -sS -o "$lookup_file" -w "%%{http_code}" \
        -X GET "$base_url/subkey/$GPD_PAYMENTS_SUB_KEY" \
        -H "Ocp-Apim-Subscription-Key: $AUTHORIZER_SERVICE_SUBKEY")

      if [ "$lookup_status" = "404" ]; then
        echo "Old authorizer config already absent (lookup HTTP 404)"
        rm -f "$lookup_file"
        exit 0
      fi

      if [ "$lookup_status" != "200" ]; then
        echo "Authorizer lookup failed: HTTP $lookup_status"
        cat "$lookup_file"
        rm -f "$lookup_file"
        exit 1
      fi

      auth_id=$(jq -r '.id // empty' "$lookup_file")
      rm -f "$lookup_file"

      if [ -z "$auth_id" ]; then
        echo "Old authorizer config already absent (no id in response)"
        exit 0
      fi

      # 2) DELETE the authorization by id.
      del_file=$(mktemp)
      del_status=$(curl -sS -o "$del_file" -w "%%{http_code}" \
        -X DELETE "$base_url/$auth_id" \
        -H "Ocp-Apim-Subscription-Key: $AUTHORIZER_SERVICE_SUBKEY")

      if [ "$del_status" = "200" ] || [ "$del_status" = "204" ] || [ "$del_status" = "404" ]; then
        echo "Deleted authorization $auth_id (HTTP $del_status)"
        rm -f "$del_file"
        exit 0
      fi

      echo "Failed to delete authorization $auth_id: HTTP $del_status"
      cat "$del_file"
      rm -f "$del_file"
      exit 1
    EOT
  }
}
