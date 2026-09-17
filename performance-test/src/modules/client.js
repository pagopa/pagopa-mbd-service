import http from 'k6/http';

const subKey = `${__ENV.API_SUBSCRIPTION_KEY}`;

export function getMBD(url, fiscalCode, idCIService) {

  var body = {
                "paymentNotices": [
                    {
                        "amount": 1600,
                        "debtor": {
                            "email": "test@pagopa.it",
                            "fullName": "Mario Rossi",
                            "uniqueIdentifier": {
                                "type": "F",
                                "value": "JHNDOE00A01B157N"
                            }
                        },
                        "documentHash": "stringstringstringstringstringstringstringst",
                        "province": "RM"
                    }
                ],
                "returnUrls": {
                    "cancelUrl": "https://url2.it",
                    "errorUrl": "https://url3.it",
                    "successUrl": "https://url1.it",
                    "waitingUrl": "https://url4.it"
                }
            };

  let headers = {
    'Ocp-Apim-Subscription-Key': subKey,
    "Content-Type": "application/json"
  };

  return http.post(url + "/" + fiscalCode + "/mbd", JSON.stringify(body), { headers});
}

export function getReceipt(url, fiscalCode,nav) {

  let headers = {
    'Ocp-Apim-Subscription-Key': subKey,
  };

  return http.get(url + "/" + fiscalCode + "/receipt/" + nav, { headers, responseType: "text"});
}
