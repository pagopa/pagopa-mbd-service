package it.gov.pagopa.mbd.service.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import it.gov.pagopa.mbd.service.exception.WebClientException;
import it.gov.pagopa.mbd.service.model.carts.CartPaymentNotice;
import it.gov.pagopa.mbd.service.model.carts.GetCartRequest;
import it.gov.pagopa.mbd.service.model.carts.GetCartResponse;
import it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp.DemandPaymentNoticeRequest;
import it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp.DemandPaymentNoticeResponse;
import it.gov.pagopa.mbd.service.model.xml.node.pafornode.CtTransferPAReceiptV2;
import it.gov.pagopa.mbd.service.model.xml.node.soap.envelope.Body;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

@SpringBootTest
class ReactiveClientTest {

    @Autowired
    ObjectMapper mapper;

    @RegisterExtension
    private static final WireMockExtension WIRE_MOCK_EXTENSION =
            WireMockExtension.newInstance().options(wireMockConfig().port(8086)).build();

    @Inject
    ReactiveClient reactiveClient;

    @Test
    void demandPaymentNoticeShouldReturnOk() {
        WIRE_MOCK_EXTENSION.stubFor(post("/demand").withHeader("Content-Type", matching(APPLICATION_XML_VALUE))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", APPLICATION_XML_VALUE)
                .withBody("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n" +
                        "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:common=\"http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/\" xmlns:nfp=\"http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd\">\n" +
                        "    <soapenv:Body>\n" +
                        "        <nfp:demandPaymentNoticeResponse>\n" +
                        "            <outcome>OK</outcome>\n" +
                        "        </nfp:demandPaymentNoticeResponse>\n" +
                        "    </soapenv:Body>\n" +
                        "</soapenv:Envelope>"
                )));
        Mono<DemandPaymentNoticeResponse> demandPaymentNoticeResponseMono = reactiveClient.demandPaymentNotice(
                DemandPaymentNoticeRequest.builder()
                        .idPSP("1212")
                        .idBrokerPSP("121212")
                        .idChannel("232323")
                        .password("")
                        .datiSpecificiServizio("test".getBytes(StandardCharsets.UTF_8))
                .build());
        DemandPaymentNoticeResponse demandPaymentNoticeResponse = demandPaymentNoticeResponseMono.block();
        assertNotNull(demandPaymentNoticeResponse);
    }

    @Test
    void demandPaymentNoticeShouldReturnKO() {
        WIRE_MOCK_EXTENSION.stubFor(post("/demand").withHeader("Content-Type", matching(APPLICATION_XML_VALUE))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", APPLICATION_XML_VALUE)
                        .withBody("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n" +
                                "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:common=\"http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/\" xmlns:nfp=\"http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd\">\n" +
                                "    <soapenv:Body>\n" +
                                "        <nfp:demandPaymentNoticeResponse>\n" +
                                "            <outcome>KO</outcome>\n" +
                                "        </nfp:demandPaymentNoticeResponse>\n" +
                                "    </soapenv:Body>\n" +
                                "</soapenv:Envelope>"
                        )));
        Mono<DemandPaymentNoticeResponse> demandPaymentNoticeResponseMono = reactiveClient.demandPaymentNotice(
                DemandPaymentNoticeRequest.builder()
                .idPSP("1212")
                .idBrokerPSP("121212")
                .idChannel("232323")
                .password("")
                .datiSpecificiServizio("test".getBytes(StandardCharsets.UTF_8))
                .build());
        assertThrows(WebClientException.class, () -> demandPaymentNoticeResponseMono.block());
    }

    @SneakyThrows
    @Test
    void getCartWithOkResponse() {
        WIRE_MOCK_EXTENSION.stubFor(post("/cart").withHeader("Content-Type", matching(APPLICATION_JSON_VALUE))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", APPLICATION_JSON_VALUE)
                        .withBody(mapper.writeValueAsString(GetCartResponse.builder().checkoutRedirectUrl("testUrl").build()))));
        Mono<GetCartResponse> getCartResponseMono = reactiveClient.getCart(
                GetCartRequest.builder().paymentNotices(Collections.singletonList(
                        CartPaymentNotice.builder().build())).build());
        GetCartResponse getCartResponse = getCartResponseMono.block();
        assertNotNull(getCartResponse);
    }


    @SneakyThrows
    @Test
    void getCartWithKOResponse() {
        WIRE_MOCK_EXTENSION.stubFor(post("/cart").withHeader("Content-Type", matching(APPLICATION_JSON_VALUE))
                .willReturn(aResponse().withStatus(500).withHeader("Content-Type", APPLICATION_JSON_VALUE)));
        Mono<GetCartResponse> getCartResponseMono = reactiveClient.getCart(
                GetCartRequest.builder().paymentNotices(Collections.singletonList(
                        CartPaymentNotice.builder().build())).build());
        assertThrows(WebClientException.class, () -> getCartResponseMono.block());
    }


    @Test
    void getPaymentReceiptWithOk() throws JsonProcessingException {
        WIRE_MOCK_EXTENSION.stubFor(get("/receipt/payments/test/receipts/test").withHeader("Content-Type", matching(APPLICATION_JSON_VALUE))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", APPLICATION_JSON_VALUE)
                        .withBody(mapper.writeValueAsString(CtTransferPAReceiptV2.builder().build()))));
        Mono<CtTransferPAReceiptV2> getResponseMono = reactiveClient.getPaymentReceipt("test","test");
        CtTransferPAReceiptV2 getResponse = getResponseMono.block();
        assertNotNull(getResponse);
    }
}