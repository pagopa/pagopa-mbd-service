import axios from "axios";

// Configuring a dedicated instance
const mbdClient = axios.create({
    baseURL: process.env.SERVICE_URI,
    headers: {
        'Ocp-Apim-Subscription-Key': process.env.SUBKEY || ""
    }
});

export async function getMDBV1(organizationFiscalCode, body) {
    let headers = {};

    return await mbdClient.post(`/v1/organizations/${organizationFiscalCode}/mbd`, body, {headers})
        .then(res => {
            return res;
        })
        .catch(error => {
            console.error("Error in getMDBV1:", error.response);
            return error.response;
        });

}

export async function getMDBV2(organizationFiscalCode, body) {
    let headers = {};

    return await mbdClient.post(`/v2/organizations/${organizationFiscalCode}/mbd`, body, {headers})
        .then(res => {
            return res;
        })
        .catch(error => {
            console.error("Error in getMDBV2:", error.response);
            return error.response;
        });

}

export async function getMdbReceiptV1(organizationFiscalCode, nav) {
    let headers = {};

    return await mbdClient.get(`/v1/organizations/${organizationFiscalCode}/receipt/${nav}`, {headers})
        .then(res => {
            return res;
        })
        .catch(error => {
            console.error("Error in getMdbReceiptV1:", error.response);
            return error.response;
        });
}

export async function getMdbReceiptV2(organizationFiscalCode, nav) {
    let headers = {};

    return await mbdClient.get(`/v2/organizations/${organizationFiscalCode}/noticeNumbers/${nav}/mbd`, {headers})
        .then(res => {
            return res;
        })
        .catch(error => {
            console.error("Error in getMdbReceiptV2:", error.response);
            return error.response;
        });
}
