const axios = require("axios");

const uri = process.env.SERVICE_URI;
const gpd_payment_uri = process.env.GPD_SERVICE_URI;
const environment = process.env.ENVIRONMENT;

axios.defaults.headers.common['Ocp-Apim-Subscription-Key'] = process.env.SUBKEY || ""; // for all requests
if (process.env.canary) {
  axios.defaults.headers.common['X-CANARY'] = 'canary' // for all requests
}


function getBody() {

	return {
        "paymentNotices": [
            {
                "firstName": "Mario",
                "lastName": "Rossi",
                "fiscalCodeEC": "77777777777",
                "amount": 16,
                "email": "test@pagopa.it",
                "province": "RM",
                "documentHash": "QUJDREVGR0hJSktMTU5PUFFSU1RVVldYWVpBQkNERQ=="
            }
        ],
        "idCIService": "01000",
        "returnUrls": {
            "successUrl": "https://url1.it",
            "cancelUrl": "https://url2.it",
            "errorUrl": "https://url3.it"
        }
	};

}

async function getDebtPositions(fiscalCodeEC, dueDate) {

    let headers = {
        "Ocp-Apim-Subscription-Key": process.env.GPD_SUBKEY;
    };


  	return await axios.get(gpd_payment_uri+"/organizations/"+fiscalCodeEC+"/debtpositions?due_date_from="+dueDate, { headers })
  		.then(res => {
  			return res.data;
  		})
  		.catch(error => {
  			return error.response;
  		});
}

async function deleteDebtPositions(fiscalCodeEC, iupd) {

    let headers = {
        "Ocp-Apim-Subscription-Key": process.env.GPD_SUBKEY;
    };


  	return await axios.delete(gpd_payment_uri+"/organizations/"+fiscalCodeEC+"/debtpositions/"+iupd, { headers })
  		.then(res => {
  			return res.data;
  		})
  		.catch(error => {
  			return error.response;
  		});
}


async function getPayPosition(fiscalCodeEC, nav) {

    let headers = {
        "Ocp-Apim-Subscription-Key": process.env.GPD_SUBKEY;
    };

  	return await axios.post(gpd_payment_uri+"/organizations/{organizationfiscalcode}/paymentoptions/{nav}", { headers })
  		.then(res => {
  			return res.data;
  		})
  		.catch(error => {
  			return error.response;
  		});
}

async function payReceipt(fiscalCodeEC, nav, body) {

    let headers = {
        "Ocp-Apim-Subscription-Key": process.env.GPD_SUBKEY;
    };

  	return await axios.post(gpd_payment_uri+"/organizations/{organizationfiscalcode}/paymentoptions/{nav}/pay", body, { headers })
  		.then(res => {
  			return res.data;
  		})
  		.catch(error => {
  			return error.response;
  		});

}

async function getMDB(fiscalCodeEC, body) {

	let headers = {};

  	return await axios.post(uri+"organizations"+fiscalCodeEC+"/mbd", body, { headers })
  		.then(res => {
  			return res.data;
  		})
  		.catch(error => {
  			return error.response;
  		});

}

async function getMdbReceipt(organizationalFiscalCode, nav) {

	let headers = {};

  	return await axios.get(uri+"/organizations/"+organizationalFiscalCode+"/receipt/"+nav, { headers })
  		.then(res => {
  			return res.data;
  		})
  		.catch(error => {
  			return error.response;
  		});

}

module.exports = {
	getMDB, getBody, getPayPosition, payReceipt, getDebtPositions, deleteDebtPosition
}
