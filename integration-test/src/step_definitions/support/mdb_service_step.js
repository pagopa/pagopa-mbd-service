const assert = require('assert');
const { Given, When, Then, After, setDefaultTimeout } = require('@cucumber/cucumber');
const {getMDB, getMdbReceipt, getBody, getPayPosition, payReceipt, getDebtPositions, deleteDebtPosition} = require("./common.js");

// set timeout for Hooks function, it allows to wait for long task
setDefaultTimeout(15 * 1000);

var fiscalCodeEC = process.env.FISCAL_CODE_EC;
var receiptNav = process.env.CORRECT_NAV;

// After each Scenario
After(async function () {

  this.response = null;

  if (this.payResponse != null) {
    var responseDebtPosition = await getDebtPositions(fiscalCodeEC, this.dueDate.split('T')[0]);
    
    for(let element of responseDebtPosition?.payment_position_list){
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
  assert.notEqual(this.response?.data?.checkoutRedirectUrl, null);
});

Then('response contains mdb link', function () {
  assert.notEqual(this.response?.data?.navDownloadLink, null);
});

Then('response contains mdb nav', function () {
  assert.notEqual(this.response?.data?.mbdNav, null);
  this.correctNav = this.response?.data?.mbdNav;
});

Given('a receipt of the former MDB payment being payed', async function () {

    this.payResponse = await getPayPosition(fiscalCodeEC, receiptNav);
    assert.notEqual(this.payResponse, null);
    this.dueDate = this.payResponse?.data?.dueDate;
//    var payBody = {
//      "paymentDate": this.response?.data?.insertedDate,
//      "paymentMethod": this.response?.data?.paymentMethod,
//      "pspCompany": this.response?.data?.pspCompany
//      "idReceipt": this.response?.data?.idReceipt,
//      "fee": this.response?.data?.fee
//    }
//    await payReceipt();

});

When('an Http GET request is sent to the mdb-service getMDBReceipt with {string}', async function (dataType) {

    switch(dataType) {
      case "correct":
          this.response = await getMdbReceipt(fiscalCodeEC, receiptNav);
          break;
      case "wrong_ec":
          this.response = await getMdbReceipt("AAAAAAA", receiptNav);
          break;
      case "wrong_nav":
          this.response = await getMdbReceipt(fiscalCodeEC, "AAAAAAAA");
          break;

    }

});

Then('response body contains content data', function () {
  assert.notEqual(this.response?.data?.content, null);
});

//COMMON

Then('response has a {int} Http status', function (expectedStatus) {
  assert.strictEqual(this.response.status, expectedStatus);
});
