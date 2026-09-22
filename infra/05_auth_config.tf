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

# Runs on every `terraform apply` (never on `plan`) to ensure the authorizer
# configuration for the subkey of gpd-payments exists. Idempotent: 200 = created,
# 409 = already present. Any other status fails the apply.
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
