locals {
  product = "${var.prefix}-${var.env_short}"

  domain         = "ebollo"
  location_short = "itn"

  apim = {
    name       = "${local.product}-apim"
    rg         = "${local.product}-api-rg"
    product_id = "pagopa_ebollo"
    hostname   = "api.${var.apim_dns_zone_prefix}.${var.external_domain}"
  }
}
