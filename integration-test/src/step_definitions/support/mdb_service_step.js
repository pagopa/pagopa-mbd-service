const assert = require('assert');
const {Given, When, Then, After, setDefaultTimeout} = require('@cucumber/cucumber');
const {insertPaymentReceiptEntity, deletePaymentReceiptEntity} = require("./gpd_payment_receipt_table_client");
const {insertDebtPosition, deleteDebtPosition} = require("./pg_gpd_client");
const {getMDBV1, getMDBV2, getMdbReceiptV1, getMdbReceiptV2} = require("./mbd_service_client.js");
const {getMDBV1Body, getMDBV2Body} = require("./common");

// set timeout for Hooks function, it allows to wait for long task
setDefaultTimeout(15 * 1000);

let fiscalCodeEC = process.env.FISCAL_CODE_EC;
let iuv = process.env.IUV;


When('an Http GET request is sent to the mdb-service getMDB V1 with {string}', async function (inputType) {
    let body = getMDBV1Body();

    switch (inputType) {
        case "valid_content":
            break;
        case "missing_fiscal_code":
            body.paymentNotices[0].fiscalCode = null;
            break;
        case "wrong_hash_document":
            body.paymentNotices[0].documentHash = "A";
            break;
    }

    this.response = await getMDBV1(fiscalCodeEC, body);
});

When('an Http GET request is sent to the mdb-service getMDB V2 with {string}', async function (inputType) {
    let body = getMDBV2Body();

    switch (inputType) {
        case "valid_content":
            break;
        case "missing_fiscal_code":
            body.paymentNotices[0].debtor.uniqueIdentifier.value = null;
            break;
        case "wrong_hash_document":
            body.paymentNotices[0].documentHash = "A";
            break;
    }

    this.response = await getMDBV2(fiscalCodeEC, body);
});

Then('response body contains checkoutUrl', function () {
    assert.notEqual(this.response?.data?.checkoutRedirectUrl, null);
});

Then('response contains mdb link', function () {
    assert.notEqual(this.response?.data?.mbdDownloadLink, null);
});

Then('response contains mdb nav', function () {
    assert.notEqual(this.response?.data?.nav, null);
    this.correctNav = this.response?.data?.nav;
});

Given('a PAID debt position stored in GPD database nav {string}', async function (nav) {
    await insertDebtPosition({iuv, fiscalCodeEC});

});

Given('a receipt stored in GPD payments table', async function () {
    await insertPaymentReceiptEntity(fiscalCodeEC, iuv);

});

When('an Http GET request is sent to the mdb-service getMDBReceipt V1 with {string}', async function (dataType) {

    switch (dataType) {
        case "correct":
            this.response = await getMdbReceiptV1(fiscalCodeEC, `3${iuv}`);
            break;
        case "wrong_ec":
            this.response = await getMdbReceiptV1("AAAAAAA", `3${iuv}`);
            break;
        case "wrong_nav":
            this.response = await getMdbReceiptV1(fiscalCodeEC, "AAAAAAAA");
            break;
    }
});

When('an Http GET request is sent to the mdb-service getMDBReceipt V2 with {string}', async function (dataType) {

    switch (dataType) {
        case "correct":
            this.response = await getMdbReceiptV2(fiscalCodeEC, `3${iuv}`);
            break;
        case "wrong_ec":
            this.response = await getMdbReceiptV2("AAAAAAA", `3${iuv}`);
            break;
        case "wrong_nav":
            this.response = await getMdbReceiptV2(fiscalCodeEC, "AAAAAAAA");
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
