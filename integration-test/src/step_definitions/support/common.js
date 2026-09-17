const axios = require("axios");

const gpd_payment_uri = process.env.GPD_SERVICE_URI;
const idCIService = process.env.CI_SERVICE || '00005';

axios.defaults.headers.common['Ocp-Apim-Subscription-Key'] = process.env.SUBKEY || ""; // for all requests
if (process.env.canary) {
  axios.defaults.headers.common['X-CANARY'] = 'canary' // for all requests
}


function getMDBV1Body() {

	return {
        "paymentNotices": [
            {
                "firstName": "Mario",
                "lastName": "Rossi",
                "fiscalCode": "JHNDOE00A01B157N",
                "amount": 1600,
                "email": "test@pagopa.it",
                "province": "RM",
                "documentHash": "PHJvb3Q+PC9yb290Pg=========================="
            }
        ],
        "idCIService": idCIService,
        "returnUrls": {
            "successUrl": "https://url1.it",
            "cancelUrl": "https://url2.it",
            "errorUrl": "https://url3.it"
        }
	};

}

function getMDBV2Body() {

	return {
        "paymentNotices": [
			{
				"amount": 1600,
				"debtor": {
					"email": "mario.rossi@test.test",
					"fullName": "Mario Rossi",
					"uniqueIdentifier": {
						"type": "F",
						"value": "JHNDOE00A01B157N"
					}
				},
				"documentHash": "PHJvb3Q+PC9yb290Pg==========================",
				"province": "RM"
			}
        ],
        "returnUrls": {
            "successUrl": "https://url1.it",
            "cancelUrl": "https://url2.it",
            "errorUrl": "https://url3.it",
			"waitingUrl": "https://url4.it"
        }
	};

}

async function getDebtPositions(fiscalCodeEC, dueDate) {

    let headers = {
        "Ocp-Apim-Subscription-Key": process.env.GPD_SUBKEY
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
        "Ocp-Apim-Subscription-Key": process.env.GPD_SUBKEY
    };

  	return await axios.delete(gpd_payment_uri+"/organizations/"+fiscalCodeEC+"/debtpositions/"+iupd, { headers })
  		.then(res => {
  			return res;
  		})
  		.catch(error => {
  			return error.response;
  		});

}

async function getPayPosition(fiscalCodeEC, nav) {

    let headers = {
        "Ocp-Apim-Subscription-Key": process.env.GPD_SUBKEY
    };

  	return await axios.get(gpd_payment_uri+"/organizations/"+fiscalCodeEC+"/paymentoptions/"+nav, { headers })
  		.then(res => {
  			return res;
  		})
  		.catch(error => {
  			return error.response;
  		});

}

async function payReceipt(fiscalCodeEC, nav, body) {

    let headers = {
        "Ocp-Apim-Subscription-Key": process.env.GPD_SUBKEY
    };

  	return await axios.post(gpd_payment_uri+"/organizations/"+fiscalCodeEC+"/paymentoptions/"+nav+"/pay", body, { headers })
  		.then(res => {
  			return res.data;
  		})
  		.catch(error => {
  			return error.response;
  		});

}


module.exports = {
	getMDBV1Body, getMDBV2Body, getPayPosition, payReceipt, getDebtPositions, deleteDebtPositions
}
