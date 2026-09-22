terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "<= 3.116.0"
    }
    azuread = {
      source  = "hashicorp/azuread"
      version = "2.30.0"
    }
    httpclient = {
      source  = "dmachard/http-client"
      version = "~> 0.1"
    }
  }

  backend "azurerm" {}
}

provider "azurerm" {
  features {}
}


data "azurerm_subscription" "current" {}

data "azurerm_client_config" "current" {}

module "__v3__" {
  # v8.103.0
  source = "git::https://github.com/pagopa/terraform-azurerm-v3?ref=d0a0b3a81963169bdc974f79eba31e41e918e63d"
}