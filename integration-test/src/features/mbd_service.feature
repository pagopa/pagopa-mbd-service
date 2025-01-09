Feature: Get MBD

  Scenario: Execute a request to getMDB service with invalid content
    When an Http GET request is sent to the mdb-service getMDB with "missing_fiscal_code"
    Then response has a 400 Http status
  
  Scenario: Execute a request to getMDB service with invalid content
    When an Http GET request is sent to the mdb-service getMDB with "wrong_hash_document"
    Then response has a 400 Http status

  Scenario: Execute a request to getMDB service with valid content
    When an Http GET request is sent to the mdb-service getMDB with "valid_content"
    Then response has a 200 Http status
    And response body contains checkoutUrl
    And response contains mdb link
    And response contains mdb nav

  Scenario: Execute a request to getMDB Receipt service with valid content
    Given a receipt of the former MDB payment being payed
    When an Http GET request is sent to the mdb-service getMDBReceipt with "correct"
    Then response has a 200 Http status

  Scenario: Execute a request to getMDBReceipt service with wrong data
    When an Http GET request is sent to the mdb-service getMDBReceipt with "wrong_ec"
    Then response has a 500 Http status

  Scenario: Execute a request to getMDBReceipt service with wrong data
    When an Http GET request is sent to the mdb-service getMDBReceipt with "wrong_nav"
    Then response has a 500 Http status