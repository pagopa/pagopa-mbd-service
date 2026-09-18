import {TableClient} from '@azure/data-tables';

const paymentReceiptTableConnString = process.env.GPD_PAYMENT_RECEIPT_TABLES_CONNECTION_STRING;
const paymentReceiptTableName = process.env.GPD_PAYMENT_RECEIPT_TABLE_NAME;

const client = TableClient.fromConnectionString(
    paymentReceiptTableConnString,
    paymentReceiptTableName
);

export async function insertPaymentReceiptEntity(organizationFiscalCode, iuv) {
    try {
        return  await client.createEntity(
        {
            partitionKey: organizationFiscalCode,
            rowKey: iuv,
            Timestamp: new Date().toISOString(),
            debtor: "JHNDOE00A01B157N",
            document: "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<PaSendRTV2Request>\n" +
                "    <idPA>15376371009</idPA>\n" +
                "    <idBrokerPA>15376371009</idBrokerPA>\n" +
                "    <idStation>15376371009_11</idStation>\n" +
                "    <receipt>\n" +
                "        <receiptId>380842b0b83f4d0fbfc1062378f20a28</receiptId>\n" +
                "        <noticeNumber>352178956907278839</noticeNumber>\n" +
                "        <fiscalCode>15376371009</fiscalCode>\n" +
                "        <outcome>OK</outcome>\n" +
                "        <creditorReferenceId>52178956907278839</creditorReferenceId>\n" +
                "        <paymentAmount>16.00</paymentAmount>\n" +
                "        <description>Pagamento marca da bollo digitale</description>\n" +
                "        <companyName>PagoPA S.p.A.</companyName>\n" +
                "        <debtor>\n" +
                "            <uniqueIdentifier>\n" +
                "                <entityUniqueIdentifierType>F</entityUniqueIdentifierType>\n" +
                "                <entityUniqueIdentifierValue>MRRNSR75R05H501I</entityUniqueIdentifierValue>\n" +
                "            </uniqueIdentifier>\n" +
                "            <fullName>mario rossi</fullName>\n" +
                "            <stateProvinceRegion>RM</stateProvinceRegion>\n" +
                "            <e-mail>francesco.cesareo@pagopa.it</e-mail>\n" +
                "        </debtor>\n" +
                "        <transferList>\n" +
                "            <transfer>\n" +
                "                <idTransfer>1</idTransfer>\n" +
                "                <transferAmount>16.00</transferAmount>\n" +
                "                <fiscalCodePA>15376371009</fiscalCodePA>\n" +
                "                <companyName> </companyName>\n" +
                "                <MBDAttachment>PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48bWFyY2FEYUJvbGxvIHhtbG5zPSJodHRwOi8vd3d3LmFnZW56aWFlbnRyYXRlLmdvdi5pdC8yMDE0L01hcmNhRGFCb2xsbyIgeG1sbnM6bnMyPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjIj48UFNQPjxDb2RpY2VGaXNjYWxlPjAwNzk5OTYwMTU4PC9Db2RpY2VGaXNjYWxlPjxEZW5vbWluYXppb25lPkludGVzYSBTYW5wYW9sbyBTLnAuQS48L0Rlbm9taW5hemlvbmU+PC9QU1A+PElVQkQ+MDEyNDAwMDIxNzU1Njc8L0lVQkQ+PE9yYUFjcXVpc3RvPjIwMjYtMDktMTZUMTQ6MzI6MTBaPC9PcmFBY3F1aXN0bz48SW1wb3J0bz4xNi4wMDwvSW1wb3J0bz48VGlwb0JvbGxvPjAxPC9UaXBvQm9sbG8+PEltcHJvbnRhRG9jdW1lbnRvPjxEaWdlc3RNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzA0L3htbGVuYyNzaGEyNTYiLz48bnMyOkRpZ2VzdFZhbHVlPjQ3REVRcGo4SEJTYSsvVEltVys1SkNldVFlUmttNU5NcEpXWkczaFN1RlU9PC9uczI6RGlnZXN0VmFsdWU+PC9JbXByb250YURvY3VtZW50bz48U2lnbmF0dXJlIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjIj48U2lnbmVkSW5mbz48Q2Fub25pY2FsaXphdGlvbk1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnL1RSLzIwMDEvUkVDLXhtbC1jMTRuLTIwMDEwMzE1Ii8+PFNpZ25hdHVyZU1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMDQveG1sZHNpZy1tb3JlI3JzYS1zaGEyNTYiLz48UmVmZXJlbmNlIFVSST0iIj48VHJhbnNmb3Jtcz48VHJhbnNmb3JtIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnI2VudmVsb3BlZC1zaWduYXR1cmUiLz48L1RyYW5zZm9ybXM+PERpZ2VzdE1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMDQveG1sZW5jI3NoYTI1NiIvPjxEaWdlc3RWYWx1ZT5nUlJ5L3RLOHNidVBkSnBiZ3ZBOTdKdkhoLzZkcjBzT2Z6djYrZldQOUdzPTwvRGlnZXN0VmFsdWU+PC9SZWZlcmVuY2U+PC9TaWduZWRJbmZvPjxTaWduYXR1cmVWYWx1ZT5LSFlHb1ZZYkY5QWNGdlpCN2VKaUZnblFnQ3FXRCtPRThkeGZ0dnJQV2lwQkFaMFJtS0k1M1VRWHV1YkpmMTh5K3U4ajYwYnRZS1RvMGl3Q2NXc0g1bndrVHhKYlpiZE5udEtoUFdBK0hPQXp3QW5rY3F2YzloS3ViNjRlMHZDbVJ3M0s1d1JSSUl5NmFaeEFNWE5TUFg5MXN6R2szdmVFSXU1aFo2WUxNdGhHSEJNS3pPTDZ2dGsvc0IxWkVRLzFVTFlnbXhMckpUblFJZTMybGdTam96N1c5d09XMDBmNlROYnIzOGlVc2JTUWdQZ3VEbisrSnROaWxzZVpwZ2tGZHp1U0R0WGtyT1Y4RVpmT3EyRmZ6ZTlNOGR6eXN4YWIySlI3ZzhkMmFGNmRrL0lIMjRKUmhEWUFFZnAwdjFBYW9vd21jcXArbjdaZGM1ZWUzdGoyRWc9PTwvU2lnbmF0dXJlVmFsdWU+PEtleUluZm8+PFg1MDlEYXRhPjxYNTA5Q2VydGlmaWNhdGU+TUlJRXRUQ0NBcDJnQXdJQkFnSUlWWGFwWjF2ODlZQXdEUVlKS29aSWh2Y05BUUVMQlFBd2FERUxNQWtHQTFVRUJoTUNTVlF4SGpBY0JnTlZCQW9NRlVGblpXNTZhV0VnWkdWc2JHVWdSVzUwY21GMFpURWJNQmtHQTFVRUN3d1NVMlZ5ZG1sNmFTQlVaV3hsYldGMGFXTnBNUnd3R2dZRFZRUUREQk5EUVNCQ2IyeHNieUJVWld4bGJXRjBhV052TUI0WERURTVNVEF3TWpFek5UZ3hOMW9YRFRJMU1UQXdNakV6TlRneE4xb3daVEVMTUFrR0ExVUVCaE1DU1ZReEhqQWNCZ05WQkFvTUZVRm5aVzU2YVdFZ1pHVnNiR1VnUlc1MGNtRjBaVEVNTUFvR0ExVUVDd3dEVUZOUU1TZ3dKZ1lEVlFRRERCOHdNRGM1T1RrMk1ERTFPQ0JKVGxSRlUwRWdVMEZPVUVGUFRFOGdVMUJCTUlJQklqQU5CZ2txaGtpRzl3MEJBUUVGQUFPQ0FROEFNSUlCQ2dLQ0FRRUF2S0dndUNIcEpCRFVXc1JiWXphTHhIN3N3VzNxNDZUZ0oraVAwRGFpV2JJL1RCZ2tDRlJydXBOTnhJNnZSNHhQaVErQ2EyeDRBMnFRY2Jtenk1U25WdTBqS3g1NnE5WUNERGI1UVF2WGR5WlJWK2lBcFY4RWdRYkhianFvMkxxUUdjVm1lSTRON296Q2VkNXBBemh3RisrTmZFMlhxaUdaV0NoNWJYYW5EazNhL3hCeS9ieFkzNDdNRkxramhObGVPb0tPY1Z0bVprTG1xK1N0SXFieVY2NnpPT213d1B4M0Z4YnQ5MzRiakVXUkZOZlZLQzE2TGQ2MHdOMHF5OGdrbkp0TjZoN01jRm1qR0dranVsVTMvKzhsZE4yOHcyeDVjTmd3cDJoRWNQcDBCZjZna1NLUUZvYU4xMW85L3dKaEsyRW02VFd6ZXFpRUFYVXEvZ0hyT1FJREFRQUJvMll3WkRBZkJnTlZIU01FR0RBV2dCUXFSN0ovSVV5L3k5NElqZEw0T2VydnI5NUNSREFTQmdOVkhTQUVDekFKTUFjR0JTdE1IUUVKTUIwR0ExVWREZ1FXQkJRcUJ5elM0U3R2dUM4Y2RXS1JzbStlWW5nRVBEQU9CZ05WSFE4QkFmOEVCQU1DQmtBd0RRWUpLb1pJaHZjTkFRRUxCUUFEZ2dJQkFBYVd0b28wR0lJcHpCeWR5bmVjMXBTaFFMZHVGS1BIeXB1UzQxc1RKd2FySVhFdGMwUHNFcnc2WmF6cVRpMGpPUkN4MTVRYm8wWWxxNEJXRTdac1NSMTRmZ1c5ZkVNU0hHUWVheHQxbjRQZUNQUjdBSll3VTZMeWswQ2FzU1pZWmdDd0VCTHhCci9oS0dNcmtpa1AyQkZuTGw1VFBwQWZYV1pZUExwYVVhOHQvUkhSbTJZZjV4ajkrR09GS3ZCYzZlOWxpRUc0MlBnOHdsN0ZWcEFJL1duREh6a0wwQjhLa0w1TTh1c3ZpQjhHdHZRR3VNMVNaN2dzcmNvbXE0OGNRU0F4L3IvV2tQeDV3c1E5SDNDSUxERkVzeU92K003eEt5YjdBUld5d0pvNUFVTmE0RjBLWmI5WUJSbUNHajdtYk1qTUxuYW1ZSnlEbWJKMjdLM1IraHhycmNsY2Z1UjI3SFkyZFRtdXRUcWpyVmZmYTNCallWdm5xQlZHaEVFNlhxU1VxdmJSYk01dVZhYVh6cFhCTzB5RktXbjRlc0NIZG1ZVVpEbWNBVHE1RDFtN2lPK3VDdE1TL0F2TFhPNnU5Y09yclFYRmhxRURHQlM3MXArNVc5SUdkN2UyejQzSXE3TUpvZ09idWxsTkhyWDUvMkxZWTNIYW4rMmRTWEJsbkZDd2UrbDlzWXRmZzFWVnpHZU5YdWEzcXNiS3VzMFN6VGdzZlJpSmNXWTNLdFhUUHplK2FhcDdCeHpWNDVxYXg5bGpTdnc4UDVzRUVDN0dic1drSXhsaVNzTDVFeTVoNmQzSmk0eit5bVp3b0NoWWNJUVFHK29OUFYzNE1TM25lSkx5V2IwSGp4bW5GbjZPSENNdDhaczNrVWQ5KzFiNzJQL0VYYTNFR2dnYzwvWDUwOUNlcnRpZmljYXRlPjxYNTA5Q1JMPk1JSUM0ekNCekFJQkFUQU5CZ2txaGtpRzl3MEJBUXNGQURCb01Rc3dDUVlEVlFRR0V3SkpWREVlTUJ3R0ExVUVDZ3dWUVdkbGJucHBZU0JrWld4c1pTQkZiblJ5WVhSbE1Sc3dHUVlEVlFRTERCSlRaWEoyYVhwcElGUmxiR1Z0WVhScFkya3hIREFhQmdOVkJBTU1FME5CSUVKdmJHeHZJRlJsYkdWdFlYUnBZMjhYRFRJMk1Ea3hOakExTWpBek5Gb1hEVEkyTURreE56QTFNakF6TTFxZ01EQXVNQjhHQTFVZEl3UVlNQmFBRkNwSHNuOGhUTC9MM2dpTjB2ZzU2dSt2M2tKRU1Bc0dBMVVkRkFRRUFnSWNnREFOQmdrcWhraUc5dzBCQVFzRkFBT0NBZ0VBUkd5V1BCSkkwUEdLNFlQNDdON0VBaXN2RUFEWDVCYXhERkhLMHhmbnZ5NmhlaXNCdDZXYUdqSGVJWG9Pdm1xZks5Ui9FQklpRkhsTTFsZnNrY214TTRjY2F2UVJTdlZYSnZVTmtleXJmMkhPcUhDeEk3VHMrUTVWWUpYY2lhOVlndXVrVkFqQkNWVE5NdzFWQS9KUlFSVmRTQ21zVFovNC9QYnhha0lZWlJ1MFozSTl5YzJ3L0E0Z0R5aTFpTE02Wm8zMllma3RGTU1sd2FKcVRYUkVtNEpSSmJsVHpvbjVmMHZOWG1ISGFrcHpoMDhuaExJN1hSS2huT0k5ZHB4VnhIR0NoMWlmMG1PZ2loQkdaZmZCYnpCYzRVUFVNeWVGOHl2YStvWWhxaXBUS1NrYXdsdS9IRWtTT215NmRBRnAyeFByTDZOZFIrUTQvOVhqTkJrTzFDcThpUDd4d0JUaVdnVUNsMVVzVDJiTWlQQURQMm1JUHlGc2pSMUFxZ3ZKREFoWE9pMmE4d3pNbGIzTUk5Z3JUSEExcVJmU0hUNUJlSDRYNGZ0bjFSSld5OGo3RlpFU3poQ0NBSHZDemRMeng0cjNMeXRXQUE2QXFwbk1EVzNqWlBOQ2ZCY2twUGJoRG5heVBDN1NkQ2ZVL1puYTNIUzhWNGViRTVianBpbmc5blpHbGljUi9JdVNieWpnTXVNbnRGUDdFOUdyOUMybjk1N0lSR1d5Tm9sbTVFb0FiSCtuSnh4Qi80SVJoMExmV0diUXBUZ08wclpoNTFrTUs1SDkzVTl6V3NhQXk2Yk5hYzMybGpGUWxndWdvKzBlRUY0bjRDZVhmUW1iRnBkcUxYcnAwUG92Uko4YVdRR0EyTUdVSHBvT3FSaHlmdmJaNFZjejM2b2lwdW89PC9YNTA5Q1JMPjwvWDUwOURhdGE+PC9LZXlJbmZvPjwvU2lnbmF0dXJlPjwvbWFyY2FEYUJvbGxvPg==</MBDAttachment>\n" +
                "                <remittanceInformation>/RFB/352178956907278839/CNR/MRRNSR75R05H501I/TXT/Pagamento marca da bollo digitale</remittanceInformation>\n" +
                "                <transferCategory>6/0811100IM/</transferCategory>\n" +
                "            </transfer>\n" +
                "        </transferList>\n" +
                "        <idPSP>BCITITMM</idPSP>\n" +
                "        <pspFiscalCode>00799960158</pspFiscalCode>\n" +
                "        <PSPCompanyName>Intesa Sanpaolo S.p.A</PSPCompanyName>\n" +
                "        <idChannel>00799960158_07</idChannel>\n" +
                "        <channelDescription>app</channelDescription>\n" +
                "        <paymentMethod>creditCard</paymentMethod>\n" +
                "        <fee>0.50</fee>\n" +
                "        <idBundle>f5f60b17-37c4-448a-8744-d5bbd3654907</idBundle>\n" +
                "        <paymentDateTime>2026-09-16T16:31:43</paymentDateTime>\n" +
                "        <applicationDate>2026-09-16</applicationDate>\n" +
                "        <transferDate>2026-09-17</transferDate>\n" +
                "        <metadata>\n" +
                "            <mapEntry>\n" +
                "                <key>NOTIFICATION_FEE</key>\n" +
                "                <value>0</value>\n" +
                "            </mapEntry>\n" +
                "        </metadata>\n" +
                "    </receipt>\n" +
                "</PaSendRTV2Request>\n",
            paymentDate: new Date().toISOString(),
            status: "PAID"
        }
    );
    } catch (err) {
        const alreadyExists = err.statusCode === 409 || err.code === "EntityAlreadyExists";
        if (alreadyExists) {
            console.error(`[gpd-payment-receipt-table] Entity already exists (partitionKey=${organizationFiscalCode}, rowKey=${iuv})`, err);
            throw new Error(
                `Payment receipt entity already exists (partitionKey=${organizationFiscalCode}, rowKey=${iuv}): ${err.message}`
            );
        }
        console.error(`[gpd-payment-receipt-table] Failed to insert entity (partitionKey=${organizationFiscalCode}, rowKey=${iuv})`, err);
        throw new Error(
            `Failed to insert payment receipt entity (partitionKey=${organizationFiscalCode}, rowKey=${iuv}): ${err.message}`
        );
    }
}

export async function deletePaymentReceiptEntity(organizationFiscalCode, iuv) {
    try {
        return  await client.deleteEntity(organizationFiscalCode, iuv);
    } catch (err) {
        // durante il cleanup l'entità potrebbe non esistere: non far fallire il teardown
        const notFound = err.statusCode === 404 || err.code === "ResourceNotFound";
        if (notFound) {
            console.warn(`[gpd-payment-receipt-table] Entity not found, skipping delete (partitionKey=${organizationFiscalCode}, rowKey=${iuv})`);
            return;
        }
        console.error(`[gpd-payment-receipt-table] Failed to delete entity (partitionKey=${organizationFiscalCode}, rowKey=${iuv})`, err);
        throw new Error(
            `Failed to delete payment receipt entity (partitionKey=${organizationFiscalCode}, rowKey=${iuv}): ${err.message}`
        );
    }
}