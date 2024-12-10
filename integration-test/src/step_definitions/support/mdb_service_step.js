const assert = require('assert');
const { Given, When, Then, After } = require('@cucumber/cucumber');
const {getMDB, getBody, getPayPosition, payReceipt, getDebtPositions, deleteDebtPosition} = require("./common.js");

const fiscalCodeEC = process.env.FISCAL_CODE_EC;

// After each Scenario
After(async function () {

  this.response = null;

  if (this.payResponse != null) {
    var responseDebtPosition = await getDebtPositions(fiscalCodeEC, dueDate);

    for(let position of responseDebtPosition?.data?.payment_position_list){
       if (element.paymentOption[0].nav === this.correctNav) {
            await deleteDebtPosition(fiscalCodeEC, element.iupd);
       }
    };
    this.correctNav = null;
    this.payResponse = null;
  }

});


When('an Http GET request is sent to the mdb-service getMDB with {string}', async function (inputType) {

  var body;

  switch(inputType) {

    case "valid_content":
        body = getBody();
        break;
    case "missing_fiscal_code":
        body = getBody();
        body.paymentNotices[0].fiscalCode = null;
        break;
    case "wrong_hash_document":
        body = getBody();
        body.paymentNotices[0].documentHash = "A";
        break;

  }

  this.response = await getMDB(fiscalCodeEC, body);

});

Then('response body contains checkoutUrl', function () {
  assert.notNull(this.response?.data?.checkoutUrl);
});

Then('response contains mdb link header', function () {
  assert.notNull(this.response?.headers?.["MDB-Link"]);
});

Then('response contains mdb nav header', function () {
  assert.notNull(this.response?.headers?.["MDB-Nav"]);
  this.correctNav = this.response?.headers?.["MDB-Nav"];
});

Given('a receipt of the former MDB payment being payed', async function () {

    this.payResponse = await getPayPosition(fiscalCodeEC, correctNav);
    assert.assertNotNull(this.response?.data);
    this.dueDate = this.response?.data?.dueDate;
    var payBody = {
      "paymentDate": this.response?.data?.insertedDate,
      "paymentMethod": this.response?.data?.paymentMethod,
      "pspCompany": this.response?.data?.pspCompany
      "idReceipt": this.response?.data?.idReceipt,
      "fee": this.response?.data?.fee
    }
    await payReceipt();

});

When('an Http GET request is sent to the mdb-service getMDBReceipt with fiscalCode {string} and {string} NAV', async function (fiscalCodeEC, navType) {

    var nav;
    switch(inputType) {

      case "correct":
          nav = this.response?.headers?.["MDB-Nav"];
          break;
      case "wrong":
          nav = "AAAAAAAA";
          break;

    }

  this.response = await getMdbReceipt(fiscalCodeEC, nav);

});

Then('response body contains content data', function () {
  assert.notNull(this.response?.data?.content);
});

//COMMON

Then('response has a {int} Http status', function (expectedStatus) {
  assert.strictEqual(this.response.status, expectedStatus);
});
