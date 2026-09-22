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

data "httpclient_request" "create_authorizer_config" {
  url            = "https://${local.apim.hostname}/shared/authorizer-config/v1/authorizations"
  request_method = "POST"

  request_headers = {
    Content-Type              = "application/json"
    Ocp-Apim-Subscription-Key = azurerm_api_management_subscription.authorizer_config_subkey.primary_key
  }

  request_body = jsonencode({
    domain           = "gpd"
    subscription_key = data.azurerm_key_vault_secret.payments_key_subscription_key.value
    description      = "Key configuration for eBollo mbd-service"
    owner = {
      id   = "15376371009"
      name = "PagoPa S.p.A"
      type = "BROKER"
    }
    authorized_entities = [
      {
        name  = "All entities"
        value = "*"
      }
    ],
    other_metadata = []
  })

  # 201 configuration created, 409 configuration already exists
  expected_status_codes = [201, 409]

  fail_on_http_error = true
}
