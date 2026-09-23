import {check} from 'k6';
import {getMBD} from './modules/client.js';
import {SharedArray} from 'k6/data';

const varsArray = new SharedArray('vars', function () {
    return JSON.parse(open(`./${__ENV.VARS}`)).environment;
});
export let options = JSON.parse(open(__ENV.TEST_TYPE));

let attachmentUrl = "";

const vars = varsArray[0];
const mbdServiceUri = `${vars.mbdServiceUri}`;
const fiscalCodeEC = `${vars.fiscalCodeEC}`;
const idCIService = `${vars.idCIService}`;


export default function () {

    let response = getMBD(mbdServiceUri, fiscalCodeEC, idCIService);

    check(response, {
        'Get MBD status is 200': (response) => response.status === 200,
        'Get MDB content_type is the expected one':
            (response) => response.headers["Content-Type"] === "application/json"
    });

}
