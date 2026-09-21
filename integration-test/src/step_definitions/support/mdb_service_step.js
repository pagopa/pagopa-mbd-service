const assert = require('node:assert');
const {When, Then, Before, AfterAll, setDefaultTimeout} = require('@cucumber/cucumber');
const {insertPaymentReceiptEntity, deletePaymentReceiptEntity} = require("./gpd_payment_receipt_table_client");
const {insertDebtPosition, deleteDebtPosition, shutDownPool} = require("./pg_gpd_client");
const {getMDBV1, getMDBV2, getMdbReceiptV1, getMdbReceiptV2} = require("./mbd_service_client.js");
const {getMDBV1Body, getMDBV2Body} = require("./common");

// set timeout for Hooks function, it allows to wait for long task
setDefaultTimeout(15 * 1000);

let organizationFiscalCode = process.env.ORGANIZATION_FISCAL_CODE;
let iuv = process.env.IUV;

// id of the payment_position created for the getMDBReceipt tests (needed for the final cleanup)
let paymentPositionId = null;

// Setup common to all getMDBReceipt scenarios: executed only once thanks to the guard.
// Inserts the PAID debt position and its related receipt into the GPD table.
Before({tags: "@getMDBReceipt"}, async function () {
    if (paymentPositionId === null) {
        const result = await insertDebtPosition({iuv, fiscalCode: organizationFiscalCode});
        paymentPositionId = result.paymentPositionId;
        await insertPaymentReceiptEntity(organizationFiscalCode, iuv);
    }
});

// Final cleanup: after all tests, delete the debt position and the receipt created for the tests.
AfterAll(async function () {
    try {
        if (paymentPositionId !== null) {
            await deleteDebtPosition(paymentPositionId);
            await deletePaymentReceiptEntity(organizationFiscalCode, iuv);
        }
    } finally {
        // Close the pg connection pool, otherwise the test process stays hanging.
        await shutDownPool();
        console.log("Connection pool closed.");
    }
});


When('an Http GET request is sent to the mdb-service getMDB V1 with {string}', async function (inputType) {
    let body = getMDBV1Body();

    switch (inputType) {
        case "valid_content":
            break;
        case "missing_fiscal_code":
            body.paymentNotices[0].fiscalCode = null;
            break;
        case "wrong_hash_document":
            body.paymentNotices[0].documentHash = null;
            break;
    }

    this.response = await getMDBV1(organizationFiscalCode, body);
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
            body.paymentNotices[0].documentHash = null;
            break;
    }

    this.response = await getMDBV2(organizationFiscalCode, body);
});

Then('response body contains checkoutUrl', function () {
    assert.notEqual(this.response?.data?.checkoutRedirectUrl, null);
});

Then('response contains mdb link', function () {
    assert.notEqual(this.response?.data?.mbdDownloadLink, null);
});

Then('response contains mdb nav', function () {
    assert.notEqual(this.response?.data?.nav, null);
});


When('an Http GET request is sent to the mdb-service getMDBReceipt V1 with {string}', async function (dataType) {

    switch (dataType) {
        case "correct":
            this.response = await getMdbReceiptV1(organizationFiscalCode, `3${iuv}`);
            break;
        case "wrong_ec":
            this.response = await getMdbReceiptV1("AAAAAAA", `3${iuv}`);
            break;
        case "wrong_nav":
            this.response = await getMdbReceiptV1(organizationFiscalCode, "AAAAAAAA");
            break;
    }
});

When('an Http GET request is sent to the mdb-service getMDBReceipt V2 with {string}', async function (dataType) {

    switch (dataType) {
        case "correct":
            this.response = await getMdbReceiptV2(organizationFiscalCode, `3${iuv}`);
            break;
        case "wrong_ec":
            this.response = await getMdbReceiptV2("AAAAAAA", `3${iuv}`);
            break;
        case "wrong_nav":
            this.response = await getMdbReceiptV2(organizationFiscalCode, "AAAAAAAA");
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
