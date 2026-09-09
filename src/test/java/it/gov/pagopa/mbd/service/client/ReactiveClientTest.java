package it.gov.pagopa.mbd.service.client;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import it.gov.pagopa.mbd.service.exception.WebClientException;
import it.gov.pagopa.mbd.service.model.carts.CartPaymentNotice;
import it.gov.pagopa.mbd.service.model.carts.GetCartRequest;
import it.gov.pagopa.mbd.service.model.carts.GetCartResponse;
import it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp.DemandPaymentNoticeRequest;
import it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp.DemandPaymentNoticeResponse;
import jakarta.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;

@SpringBootTest
class ReactiveClientTest {

  @Autowired ObjectMapper mapper;

  @RegisterExtension
  private static final WireMockExtension WIRE_MOCK_EXTENSION =
      WireMockExtension.newInstance().options(wireMockConfig().port(8086)).build();

  @Inject ReactiveClient reactiveClient;

  @Test
  void demandPaymentNoticeShouldReturnOk() {
    WIRE_MOCK_EXTENSION.stubFor(
        post("/demand")
            .withHeader("Content-Type", matching(APPLICATION_XML_VALUE))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", APPLICATION_XML_VALUE)
                    .withBody(
"""
<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
  <soapenv:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:common="http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/" xmlns:nfp="http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd">
    <soapenv:Body>
        <nfp:demandPaymentNoticeResponse>
            <outcome>OK</outcome>
        </nfp:demandPaymentNoticeResponse>
    </soapenv:Body>
</soapenv:Envelope>
""")));
    Mono<DemandPaymentNoticeResponse> demandPaymentNoticeResponseMono =
        reactiveClient.demandPaymentNotice(
            DemandPaymentNoticeRequest.builder()
                .idPSP("1212")
                .idBrokerPSP("121212")
                .idChannel("232323")
                .password("")
                .datiSpecificiServizio("test".getBytes(StandardCharsets.UTF_8))
                .build());
    DemandPaymentNoticeResponse demandPaymentNoticeResponse =
        demandPaymentNoticeResponseMono.block();
    assertNotNull(demandPaymentNoticeResponse);
  }

  @Test
  void demandPaymentNoticeShouldReturnKO() {
    WIRE_MOCK_EXTENSION.stubFor(
        post("/demand")
            .withHeader("Content-Type", matching(APPLICATION_XML_VALUE))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", APPLICATION_XML_VALUE)
                    .withBody(
                        """
                                <?xml version="1.0" encoding="UTF-8" standalone="no" ?>
                                  <soapenv:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:common="http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/" xmlns:nfp="http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd">
                                    <soapenv:Body>
                                        <nfp:demandPaymentNoticeResponse>
                                            <outcome>KO</outcome>
                                        </nfp:demandPaymentNoticeResponse>
                                    </soapenv:Body>
                                </soapenv:Envelope>
                                """)));
    Mono<DemandPaymentNoticeResponse> demandPaymentNoticeResponseMono =
        reactiveClient.demandPaymentNotice(
            DemandPaymentNoticeRequest.builder()
                .idPSP("1212")
                .idBrokerPSP("121212")
                .idChannel("232323")
                .password("")
                .datiSpecificiServizio("test".getBytes(StandardCharsets.UTF_8))
                .build());
    assertThrows(WebClientException.class, demandPaymentNoticeResponseMono::block);
  }

  @SneakyThrows
  @Test
  void getCartWithOkResponse() {
    WIRE_MOCK_EXTENSION.stubFor(
        post("/cart")
            .withHeader("Content-Type", matching(APPLICATION_JSON_VALUE))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                    .withBody(
                        mapper.writeValueAsString(
                            GetCartResponse.builder().checkoutRedirectUrl("testUrl").build()))));
    Mono<GetCartResponse> getCartResponseMono =
        reactiveClient.getCart(
            GetCartRequest.builder()
                .paymentNotices(Collections.singletonList(CartPaymentNotice.builder().build()))
                .build());
    GetCartResponse getCartResponse = getCartResponseMono.block();
    assertNotNull(getCartResponse);
  }

  @SneakyThrows
  @Test
  void getCartWithKOResponse() {
    WIRE_MOCK_EXTENSION.stubFor(
        post("/cart")
            .withHeader("Content-Type", matching(APPLICATION_JSON_VALUE))
            .willReturn(
                aResponse().withStatus(500).withHeader("Content-Type", APPLICATION_JSON_VALUE)));
    Mono<GetCartResponse> getCartResponseMono =
        reactiveClient.getCart(
            GetCartRequest.builder()
                .paymentNotices(Collections.singletonList(CartPaymentNotice.builder().build()))
                .build());
    assertThrows(WebClientException.class, getCartResponseMono::block);
  }

  @Test
  void getPaymentReceiptWithOk() {
    WIRE_MOCK_EXTENSION.stubFor(
        get(urlMatching("/receipt/.*"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", APPLICATION_XML_VALUE)
                    .withBody(
"""
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<PaSendRTV2Request>
    <idPA>99999000013</idPA>
    <idBrokerPA>15376371009</idBrokerPA>
    <idStation>15376371009_48</idStation>
    <receipt>
        <receiptId>7d7cfaeb94a742bda3c8d389a3259293</receiptId>
        <noticeNumber>348173447377455101</noticeNumber>
        <fiscalCode>99999000013</fiscalCode>
        <outcome>OK</outcome>
        <creditorReferenceId>48173447377455101</creditorReferenceId>
        <paymentAmount>16.00</paymentAmount>
        <description>Pagamento marca da bollo digitale</description>
        <companyName>EC DEMO</companyName>
        <debtor>
            <uniqueIdentifier>
               <entityUniqueIdentifierType>F</entityUniqueIdentifierType>
               <entityUniqueIdentifierValue>ANONIMO</entityUniqueIdentifierValue>
            </uniqueIdentifier>
            <fullName>ANONIMO</fullName>
        </debtor>
        <transferList>
            <transfer>
                <idTransfer>1</idTransfer>
                <transferAmount>16.00</transferAmount>
                <fiscalCodePA>99999000013</fiscalCodePA>
                <companyName> </companyName>
                <MBDAttachment>PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48bWFyY2FEYUJvbGxvIHhtbG5zPSJodHRwOi8vd3d3LmFnZW56aWFlbnRyYXRlLmdvdi5pdC8yMDE0L01hcmNhRGFCb2xsbyIgeG1sbnM6bnMyPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjIj48UFNQPjxDb2RpY2VGaXNjYWxlPjAwNzk5OTYwMTU4PC9Db2RpY2VGaXNjYWxlPjxEZW5vbWluYXppb25lPkludGVzYSBTYW5wYW9sbyBTLnAuQS48L0Rlbm9taW5hemlvbmU+PC9QU1A+PElVQkQ+MDEyNDAwMDIxODA2NDY8L0lVQkQ+PE9yYUFjcXVpc3RvPjIwMjQtMDctMDhUMjA6MTg6MDErMDI6MDA8L09yYUFjcXVpc3RvPjxJbXBvcnRvPjE2LjAwPC9JbXBvcnRvPjxUaXBvQm9sbG8+MDE8L1RpcG9Cb2xsbz48SW1wcm9udGFEb2N1bWVudG8+PERpZ2VzdE1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMDQveG1sZW5jI3NoYTI1NiIvPjxuczI6RGlnZXN0VmFsdWU+MXRyQTVxeWpTWk53aXd0R0c0NmR5alJwTDE2VEZnR0NGdm5mRnpRckZIYkI8L25zMjpEaWdlc3RWYWx1ZT48L0ltcHJvbnRhRG9jdW1lbnRvPjxTaWduYXR1cmUgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyMiPjxTaWduZWRJbmZvPjxDYW5vbmljYWxpemF0aW9uTWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvVFIvMjAwMS9SRUMteG1sLWMxNG4tMjAwMTAzMTUiLz48U2lnbmF0dXJlTWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8wNC94bWxkc2lnLW1vcmUjcnNhLXNoYTI1NiIvPjxSZWZlcmVuY2UgVVJJPSIiPjxUcmFuc2Zvcm1zPjxUcmFuc2Zvcm0gQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjZW52ZWxvcGVkLXNpZ25hdHVyZSIvPjwvVHJhbnNmb3Jtcz48RGlnZXN0TWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8wNC94bWxlbmMjc2hhMjU2Ii8+PERpZ2VzdFZhbHVlPlI4bGZHeUluMFZENCtUV0NSajJKM0pkTkY3MWxDYk9USDNkYVRPVVVGRWs9PC9EaWdlc3RWYWx1ZT48L1JlZmVyZW5jZT48L1NpZ25lZEluZm8+PFNpZ25hdHVyZVZhbHVlPllWbFpLVk90WXRPYjYvamNUSUFCUFpkZThNVE9UZnZXdURSemhhZFY0S2czWHFoU2JBNUhEd042MmNDNEJIL0dzNGpMdHlnSGxzMFVHdXd6RngyUWJnZTU1b1lCZmV6d3RveDVZL0lGNDM2bk5KWkxYNXY0bjJwd1A3Z3ZZYWp2WERKNVdSaEswOXZmblZoRkU1Z2pEOUhGNEVwa3hyaDRkeWpHUU1rN2dCVVdIR0YrN3ZHUzV1U1dxc2RReGVIemhMSnZKMWx6Qi8zVjJsZHR5emd0M2tzd1FHTkUyYmtGWmZWZTFOK0wreTQ1MUpVZWNtK05hdndxbHdscU1KUDU2SnhlTE5JSzZRaVVrWmtMTU9TSW8zT1FIMjM0b3BVOTRtSFhTcUdaMXdmVmVhRVdzUWphY3JwUjBWd0FWV1hNNWttblp5Qy9oMHcyUTlnYUVuUnJpUT09PC9TaWduYXR1cmVWYWx1ZT48S2V5SW5mbz48WDUwOURhdGE+PFg1MDlDZXJ0aWZpY2F0ZT5NSUlFdFRDQ0FwMmdBd0lCQWdJSVZYYXBaMXY4OVlBd0RRWUpLb1pJaHZjTkFRRUxCUUF3YURFTE1Ba0dBMVVFQmhNQ1NWUXhIakFjQmdOVkJBb01GVUZuWlc1NmFXRWdaR1ZzYkdVZ1JXNTBjbUYwWlRFYk1Ca0dBMVVFQ3d3U1UyVnlkbWw2YVNCVVpXeGxiV0YwYVdOcE1Sd3dHZ1lEVlFRRERCTkRRU0JDYjJ4c2J5QlVaV3hsYldGMGFXTnZNQjRYRFRFNU1UQXdNakV6TlRneE4xb1hEVEkxTVRBd01qRXpOVGd4TjFvd1pURUxNQWtHQTFVRUJoTUNTVlF4SGpBY0JnTlZCQW9NRlVGblpXNTZhV0VnWkdWc2JHVWdSVzUwY21GMFpURU1NQW9HQTFVRUN3d0RVRk5RTVNnd0pnWURWUVFEREI4d01EYzVPVGsyTURFMU9DQkpUbFJGVTBFZ1UwRk9VRUZQVEU4Z1UxQkJNSUlCSWpBTkJna3Foa2lHOXcwQkFRRUZBQU9DQVE4QU1JSUJDZ0tDQVFFQXZLR2d1Q0hwSkJEVVdzUmJZemFMeEg3c3dXM3E0NlRnSitpUDBEYWlXYkkvVEJna0NGUnJ1cE5OeEk2dlI0eFBpUStDYTJ4NEEycVFjYm16eTVTblZ1MGpLeDU2cTlZQ0REYjVRUXZYZHlaUlYraUFwVjhFZ1FiSGJqcW8yTHFRR2NWbWVJNE43b3pDZWQ1cEF6aHdGKytOZkUyWHFpR1pXQ2g1YlhhbkRrM2EveEJ5L2J4WTM0N01GTGtqaE5sZU9vS09jVnRtWmtMbXErU3RJcWJ5VjY2ek9PbXd3UHgzRnhidDkzNGJqRVdSRk5mVktDMTZMZDYwd04wcXk4Z2tuSnRONmg3TWNGbWpHR2tqdWxVMy8rOGxkTjI4dzJ4NWNOZ3dwMmhFY1BwMEJmNmdrU0tRRm9hTjExbzkvd0poSzJFbTZUV3plcWlFQVhVcS9nSHJPUUlEQVFBQm8yWXdaREFmQmdOVkhTTUVHREFXZ0JRcVI3Si9JVXkveTk0SWpkTDRPZXJ2cjk1Q1JEQVNCZ05WSFNBRUN6QUpNQWNHQlN0TUhRRUpNQjBHQTFVZERnUVdCQlFxQnl6UzRTdHZ1QzhjZFdLUnNtK2VZbmdFUERBT0JnTlZIUThCQWY4RUJBTUNCa0F3RFFZSktvWklodmNOQVFFTEJRQURnZ0lCQUFhV3RvbzBHSUlwekJ5ZHluZWMxcFNoUUxkdUZLUEh5cHVTNDFzVEp3YXJJWEV0YzBQc0VydzZaYXpxVGkwak9SQ3gxNVFibzBZbHE0QldFN1pzU1IxNGZnVzlmRU1TSEdRZWF4dDFuNFBlQ1BSN0FKWXdVNkx5azBDYXNTWllaZ0N3RUJMeEJyL2hLR01ya2lrUDJCRm5MbDVUUHBBZlhXWllQTHBhVWE4dC9SSFJtMllmNXhqOStHT0ZLdkJjNmU5bGlFRzQyUGc4d2w3RlZwQUkvV25ESHprTDBCOEtrTDVNOHVzdmlCOEd0dlFHdU0xU1o3Z3NyY29tcTQ4Y1FTQXgvci9Xa1B4NXdzUTlIM0NJTERGRXN5T3YrTTd4S3liN0FSV3l3Sm81QVVOYTRGMEtaYjlZQlJtQ0dqN21iTWpNTG5hbVlKeURtYkoyN0szUitoeHJyY2xjZnVSMjdIWTJkVG11dFRxanJWZmZhM0JqWVZ2bnFCVkdoRUU2WHFTVXF2YlJiTTV1VmFhWHpwWEJPMHlGS1duNGVzQ0hkbVlVWkRtY0FUcTVEMW03aU8rdUN0TVMvQXZMWE82dTljT3JyUVhGaHFFREdCUzcxcCs1VzlJR2Q3ZTJ6NDNJcTdNSm9nT2J1bGxOSHJYNS8yTFlZM0hhbisyZFNYQmxuRkN3ZStsOXNZdGZnMVZWekdlTlh1YTNxc2JLdXMwU3pUZ3NmUmlKY1dZM0t0WFRQemUrYWFwN0J4elY0NXFheDlsalN2dzhQNXNFRUM3R2JzV2tJeGxpU3NMNUV5NWg2ZDNKaTR6K3ltWndvQ2hZY0lRUUcrb05QVjM0TVMzbmVKTHlXYjBIanhtbkZuNk9IQ010OFpzM2tVZDkrMWI3MlAvRVhhM0VHZ2djPC9YNTA5Q2VydGlmaWNhdGU+PFg1MDlDUkw+TUlJQzR6Q0J6QUlCQVRBTkJna3Foa2lHOXcwQkFRc0ZBREJvTVFzd0NRWURWUVFHRXdKSlZERWVNQndHQTFVRUNnd1ZRV2RsYm5wcFlTQmtaV3hzWlNCRmJuUnlZWFJsTVJzd0dRWURWUVFMREJKVFpYSjJhWHBwSUZSbGJHVnRZWFJwWTJreEhEQWFCZ05WQkFNTUUwTkJJRUp2Ykd4dklGUmxiR1Z0WVhScFkyOFhEVEkwTVRJeE56RXpNekkxTWxvWERUSTBNVEl4T0RFek16STFNVnFnTURBdU1COEdBMVVkSXdRWU1CYUFGQ3BIc244aFRML0wzZ2lOMHZnNTZ1K3Yza0pFTUFzR0ExVWRGQVFFQWdJWGZqQU5CZ2txaGtpRzl3MEJBUXNGQUFPQ0FnRUFkMTNsZWF2QXlDc3NabWx0UDg3K0FwV3hESnlta3l5djJvVlVTL0RSclRJeWxmUlJEb2dzNWpZbVRENzA2ZWlLeU5yNzRWU0VFOFkyanVtOEdFckVPdnBjd1JxSzNyRUFjTXExSE81WXZOMnkrRjlVdncwb2ZQZUllU0R0NDVQbEw4WHoydGF4UE5qV3JFMUNFQVUyQ3VHT1ljRlpERVpPRlllcjJ2d2JlTjQ2bUo3aGFjM09zNnRtRWpwVnZya2hjaVk4RlkvdUxNbmZPZE9vam1SU3pUM3Nyek9LbkEyVUxiaHc0VGp0UFBIbFdyd0ZFSFowR1A0eFNZdEoxTnhxeTFtZ21BVW1jd3I5ckI4NElOZmlXUy9ORGlmVmpzYUlNeW9PNG1OTGw0WmdCZUtkUlFIVG9zYUJob29tMitMdjIzNjAybEsvNzVUVE5zblZlb08wUWVMZGpSd2x5citoNDZZLzB3NFRDZmNRMHNpVktGcC8ya1pGZktFOU1YeFlQdmhtQnF4VVJHbHM1L1g4VzVzT2cyUER4M1ViZkdoMi9LODZqRklScmxrQmNISmFjNENiODV3aWQ0bUo3aDNJK2R3dmtUNUxCK2tWcURkMGZuQURxTDEvaGwvT0lhRW1JdzNQanRzSDlwSHQ2UjdzY2lXN242ZmtwSG9ZOXB2dk9wZlhmcGVvTFVMSGpWbUhhUnFkN0dkc21GSjdFbmpYblVOWXJpNDZKYkh0ZUNsM3AvNk9mcFY3NUtjTVNIVGlJYks5SkFITFBGNlpDOUdycEJuT0s2eTBmUTZqWm5yNWNVQ2tESFNMQlprYkx5Kzhzd1liYnI5T2RwbnRlL1JOS1BPQ2VDVmdnQzBhN0VOaXBKeko3K2plaW03cktIKzB0ak1XSExlRUdCND08L1g1MDlDUkw+PC9YNTA5RGF0YT48L0tleUluZm8+PC9TaWduYXR1cmU+PC9tYXJjYURhQm9sbG8+</MBDAttachment>
                <remittanceInformation>Pagamento marca da bollo digitale</remittanceInformation>
                <transferCategory>9/0702103TS/</transferCategory>
            </transfer>
        </transferList>
        <idPSP>BCITITMM</idPSP>
        <pspFiscalCode>00799960158</pspFiscalCode>
        <PSPCompanyName>Intesa Sanpaolo S.p.A</PSPCompanyName>
        <idChannel>00799960158_01_ONUS</idChannel>
        <channelDescription>app</channelDescription>
        <paymentMethod>creditCard</paymentMethod>
        <fee>0.50</fee>
        <idBundle>c47c5e72-ec53-4be7-bcbb-ad7554ba13d7</idBundle>
        <paymentDateTime>2024-12-17T23:18:16</paymentDateTime>
        <applicationDate>2024-12-17</applicationDate>
        <transferDate>2024-12-18</transferDate>
    </receipt>
</PaSendRTV2Request>
""")));
    Mono<byte[]> responseMono = reactiveClient.getPaymentReceipt("test", "test");
    byte[] response = responseMono.block();
    assertNotNull(response);
  }

  @Test
  void getPaymentReceiptWithKO() {
    WIRE_MOCK_EXTENSION.stubFor(
        get(urlMatching("/receipt/.*")).willReturn(aResponse().withStatus(500)));
    Mono<byte[]> getResponseMono = reactiveClient.getPaymentReceipt("test", "test");
    assertThrows(WebClientException.class, getResponseMono::block);
  }
}
