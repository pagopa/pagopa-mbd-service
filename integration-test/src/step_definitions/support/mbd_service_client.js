import axios from "axios";

// Configuring a dedicated instance
const mbdClient = axios.create({
    baseURL: process.env.SERVICE_URI,
    headers: {
        'Ocp-Apim-Subscription-Key': process.env.SUBKEY || ""
    }
});

async function getMDBV1(fiscalCodeEC, body) {
    let headers = {};

    return await mbdClient.post(`/v1/organizations/${fiscalCodeEC}/mbd`, body, {headers})
        .then(res => {
            return res;
        })
        .catch(error => {
            return error.response;
        });

}

async function getMDBV2(fiscalCodeEC, body) {
    let headers = {};

    return await mbdClient.post(`/v2/organizations/${fiscalCodeEC}/mbd`, body, {headers})
        .then(res => {
            return res;
        })
        .catch(error => {
            return error.response;
        });

}

async function getMdbReceiptV1(organizationalFiscalCode, nav) {
    let headers = {};

    return await mbdClient.get(`/v1/organizations/${organizationalFiscalCode}/receipt/${nav}`, {headers})
        .then(res => {
            return res;
        })
        .catch(error => {
            return error.response;
        });
}

async function getMdbReceiptV2(organizationalFiscalCode, nav) {
    let headers = {};

    return await mbdClient.get(`/v2/organizations/${organizationalFiscalCode}/noticeNumbers/${nav}/mbd`, {headers})
        .then(res => {
            return res;
        })
        .catch(error => {
            return error.response;
        });
}

export {getMDBV1, getMDBV2, getMdbReceiptV1, getMdbReceiptV2};