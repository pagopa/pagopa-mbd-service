package it.gov.pagopa.mbd.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.mbd.service.model.carts.GetCartErrorResponse;
import it.gov.pagopa.mbd.service.model.carts.GetCartResponse;
import it.gov.pagopa.mbd.service.model.mdb.GetMbdRequest;
import it.gov.pagopa.mbd.service.model.mdb.PaymentNotice;
import it.gov.pagopa.mbd.service.model.mdb.ReturnUrls;
import it.gov.pagopa.mbd.service.service.MbdService;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = MbdController.class)
class MbdControllerTest {

    @MockBean
    private MbdService mbdService;

    @Autowired
    private WebTestClient webClient;

    @BeforeEach
    void setUp() {
        Mockito.reset(mbdService);
    }

    @Inject
    ObjectMapper objectMapper;

    @Test
    void getMdbShouldReturnCheckoutUrlOnPositiveRequest() throws Exception {
        when(mbdService.getMbd(any())).thenAnswer(item ->
                Mono.just(ResponseEntity.ok().body(GetCartResponse.builder()
                        .checkoutRedirectUrl("testUrl").build())));
        webClient.post().uri("/mbd").bodyValue(
                objectMapper.writeValueAsBytes(GetMbdRequest.builder()
                        .idCIService("test")
                        .paymentNotices(Collections.singletonList(
                                PaymentNotice.builder().build()
                        ))
                        .returnUrls(ReturnUrls.builder().build())
                        .build())).header("Content-Type",MediaType.APPLICATION_JSON_VALUE)
                .exchange().expectStatus().is2xxSuccessful()
                .expectBody(GetCartResponse.class).consumeWith(result -> {
                    GetCartResponse getCartResponse = result.getResponseBody();
                    assertNotNull(getCartResponse);
                    assertNotNull(getCartResponse.getCheckoutRedirectUrl());
                    assertEquals(getCartResponse.getCheckoutRedirectUrl(), "testUrl");
                });

    }

    @Test
    void getMdbShouldReturnErrorUrlOnKoRequest() throws Exception {
        when(mbdService.getMbd(any())).thenAnswer(item ->
                Mono.error(new RuntimeException("")));
        webClient.post().uri("/mbd").bodyValue(
                        objectMapper.writeValueAsBytes(GetMbdRequest.builder()
                                .idCIService("test")
                                .paymentNotices(Collections.singletonList(
                                        PaymentNotice.builder().build()
                                ))
                                .returnUrls(ReturnUrls.builder().errorUrl("testUrl").build())
                                .build())).header("Content-Type",MediaType.APPLICATION_JSON_VALUE)
                .exchange().expectStatus().is5xxServerError()
                .expectBody(GetCartErrorResponse.class)
                .consumeWith(result -> {
                    GetCartErrorResponse getCartResponse = result.getResponseBody();
                    assertNotNull(getCartResponse);
                    assertNotNull(getCartResponse.getErrorUrl());
                    assertEquals(getCartResponse.getErrorUrl(), "testUrl");
                });
    }

    @Test
    void getPaymentReceiptsShouldRetunrContentOnValidCall() throws Exception {
        when(mbdService.getPaymentReceipts(any(),any())).thenAnswer(item ->
                Mono.just(ResponseEntity.ok().body("ABC".getBytes())));
        webClient.get().uri("/mbd-payments/test/receipt/30000000001")
                .exchange().expectStatus().is2xxSuccessful()
                .expectBody(String.class)
                .consumeWith(result -> {
                    String body = result.getResponseBody();
                    assertNotNull(body);
                    assertEquals(body, "ABC");
                });
    }

}