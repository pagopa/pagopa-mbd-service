Feature: @e.Bollo 2.0 APIs

  Scenario: Execute a request to getMDB V1 API with invalid debtor fiscal code
    When an Http GET request is sent to the mdb-service getMDB V1 with "missing_fiscal_code"
    Then response has a 400 Http status

  Scenario: Execute a request to getMDB V1 API with invalid hash document
    When an Http GET request is sent to the mdb-service getMDB V1 with "wrong_hash_document"
    Then response has a 400 Http status

  Scenario: Execute a request to getMDB V1 API with valid content
    When an Http GET request is sent to the mdb-service getMDB V1 with "valid_content"
    Then response has a 200 Http status
    And response body contains checkoutUrl
    And response contains mdb link
    And response contains mdb nav

  Scenario: Execute a request to getMDB V2 API with invalid debtor fiscal code
    When an Http GET request is sent to the mdb-service getMDB V2 with "missing_fiscal_code"
    Then response has a 400 Http status

  Scenario: Execute a request to getMDB V2 API with invalid hash document
    When an Http GET request is sent to the mdb-service getMDB V2 with "wrong_hash_document"
    Then response has a 400 Http status

  Scenario: Execute a request to getMDB V2 API with valid content
    When an Http GET request is sent to the mdb-service getMDB V2 with "valid_content"
    Then response has a 200 Http status
    And response body contains checkoutUrl
    And response contains mdb link
    And response contains mdb nav

  Scenario: Execute a request to getMDB Receipt V1 API with valid content
    Given a PAID debt position stored in GPD database nav 352178956907266666
    And a receipt stored in GPD payments table
    When an Http GET request is sent to the mdb-service getMDBReceipt V1 with "correct"
    Then response has a 200 Http status

  Scenario: Execute a request to getMDBReceipt V1 API with wrong data
    When an Http GET request is sent to the mdb-service getMDBReceipt V2 with "wrong_ec"
    Then response has a 500 Http status

  Scenario: Execute a request to getMDBReceipt V1 API with wrong data
    When an Http GET request is sent to the mdb-service getMDBReceipt V1 with "wrong_nav"
    Then response has a 500 Http status

  Scenario: Execute a request to getMDB Receipt V2 API with valid content
    Given a receipt of the former MDB payment being payed
    When an Http GET request is sent to the mdb-service getMDBReceipt V2 with "correct"
    Then response has a 200 Http status

  Scenario: Execute a request to getMDBReceipt V2 API with wrong data
    When an Http GET request is sent to the mdb-service getMDBReceipt V2 with "wrong_ec"
    Then response has a 500 Http status

  Scenario: Execute a request to getMDBReceipt V2 API with wrong data
    When an Http GET request is sent to the mdb-service getMDBReceipt V2 with "wrong_nav"
    Then response has a 500 Http status