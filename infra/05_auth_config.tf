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
      APIM_HOSTNAME    = local.apim.hostname
      APIM_SUB_KEY     = azurerm_api_management_subscription.authorizer_config_subkey.primary_key
      GPD_PAYMENTS_SUB_KEY = data.azurerm_key_vault_secret.payments_key_subscription_key.value
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
        -H "Ocp-Apim-Subscription-Key: $APIM_SUB_KEY" \
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
#   - `triggers` captures the current subkey, APIM hostname, and APIM
#     subscription key. When any trigger changes, Terraform replaces the
#     resource: it destroys the OLD instance (running the destroy provisioner
#     with the OLD trigger values via `self.triggers`) and creates a new one.
#   - Destroy provisioner:
#       1. GET /authorizations/subkey/{oldSubkey} to obtain the id(s).
#       2. DELETE /authorizations/{id} for each match.
#     Accepted statuses: 200/204 (deleted), 404 (already gone), empty list
#     (nothing to do). Anything else fails the apply.
#   - Create is a no-op; the row for the NEW subkey is (re)created by
#     `create_authorizer_config` on the same apply.
#
# Side effects / things to be aware of:
#   - Terraform forbids referencing anything except `self`, `count`, or `each`
#     inside a destroy provisioner. That's why hostname and APIM subscription
#     key are duplicated into `triggers` — they must be available on the
#     resource being destroyed, whose state holds the OLD values.
#   - Rotating the APIM subscription key of `authorizer_config_subkey` (e.g.,
#     from the Azure portal) also triggers replacement of this resource. The
#     destroy provisioner then DELETEs the current authorizer row and
#     `create_authorizer_config` re-POSTs it on the same apply. Functionally
#     correct but produces an extra DELETE+POST cycle on the authorizer DB.
#   - The hostname trigger never changes in practice (fixed per environment),
#     so it does not cause spurious replacements.
#   - On `terraform destroy` of this stack, the destroy provisioner fires and
#     removes the authorizer row. This is the correct teardown behavior but
#     means destroying the stack revokes access for mbd-service.
#   - If the GET returns multiple authorizations for the same subkey (edge
#     case), all of them are deleted. This is defensive and matches the
#     "one subkey = one config" invariant.
# -----------------------------------------------------------------------------
resource "null_resource" "cleanup_authorizer_config" {
  triggers = {
    subkey        = data.azurerm_key_vault_secret.payments_key_subscription_key.value
    apim_hostname = local.apim.hostname
    apim_sub_key  = azurerm_api_management_subscription.authorizer_config_subkey.primary_key
  }

  provisioner "local-exec" {
    when        = destroy
    interpreter = ["bash", "-c"]

    environment = {
      APIM_HOSTNAME        = self.triggers.apim_hostname
      APIM_SUB_KEY         = self.triggers.apim_sub_key
      GPD_PAYMENTS_SUB_KEY = self.triggers.subkey
    }

    command = <<-EOT
      set -euo pipefail

      base_url="https://$APIM_HOSTNAME/shared/authorizer-config/v1/authorizations"

      # 1) Lookup authorization(s) by subkey to obtain the id(s).
      lookup_file=$(mktemp)
      lookup_status=$(curl -sS -o "$lookup_file" -w "%%{http_code}" \
        -X GET "$base_url/subkey/$GPD_PAYMENTS_SUB_KEY" \
        -H "Ocp-Apim-Subscription-Key: $APIM_SUB_KEY")

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

      ids=$(jq -r '.authorizations[]?.id // empty' "$lookup_file")
      rm -f "$lookup_file"

      if [ -z "$ids" ]; then
        echo "Old authorizer config already absent (empty authorizations)"
        exit 0
      fi

      # 2) DELETE each authorization by id.
      while IFS= read -r auth_id; do
        [ -z "$auth_id" ] && continue
        del_file=$(mktemp)
        del_status=$(curl -sS -o "$del_file" -w "%%{http_code}" \
          -X DELETE "$base_url/$auth_id" \
          -H "Ocp-Apim-Subscription-Key: $APIM_SUB_KEY")
        if [ "$del_status" = "200" ] || [ "$del_status" = "204" ] || [ "$del_status" = "404" ]; then
          echo "Deleted authorization $auth_id (HTTP $del_status)"
          rm -f "$del_file"
          continue
        fi
        echo "Failed to delete authorization $auth_id: HTTP $del_status"
        cat "$del_file"
        rm -f "$del_file"
        exit 1
      done <<< "$ids"

      echo "Old authorizer config cleanup OK"
    EOT
  }
}
