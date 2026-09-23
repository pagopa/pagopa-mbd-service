const idCIService = process.env.CI_SERVICE || '00005';

export function getMDBV1Body() {
    return {
        "paymentNotices": [
            {
                "firstName": "John",
                "lastName": "Doe",
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

export function getMDBV2Body() {
    return {
        "paymentNotices": [
            {
                "amount": 1600,
                "debtor": {
                    "email": "john.doe@test.test",
                    "fullName": "John Doe",
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