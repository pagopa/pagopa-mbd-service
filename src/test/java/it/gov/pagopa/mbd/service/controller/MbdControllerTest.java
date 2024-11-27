package it.gov.pagopa.mbd.service.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.mbd.service.model.carts.GetCartRequest;
import it.gov.pagopa.mbd.service.model.carts.GetCartResponse;
import it.gov.pagopa.mbd.service.model.mdb.GetMdbRequest;
import it.gov.pagopa.mbd.service.model.mdb.PaymentNotice;
import it.gov.pagopa.mbd.service.model.mdb.ReturnUrls;
import it.gov.pagopa.mbd.service.service.MdbService;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = MbdController.class)
class MbdControllerTest {

    @MockBean
    private MdbService mbdService;

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
        when(mbdService.getMdb(any())).thenAnswer(item ->
                Mono.just(ServerResponse.ok().bodyValue(GetCartResponse.builder()
                        .checkoutRedirectUrl("testUrl").build())));
        webClient.post().uri("/mbd").bodyValue(
                objectMapper.writeValueAsBytes(GetMdbRequest.builder()
                        .idCiService("test")
                        .paymentNotices(Collections.singletonList(
                                PaymentNotice.builder().build()
                        ))
                        .returnUrls(ReturnUrls.builder().build())
                        .build())).header("Content-Type",MediaType.APPLICATION_JSON_VALUE)
                .exchange().expectStatus().is2xxSuccessful();
        //                .consumeWith(result -> {
//                    GetCartResponse getCartResponse = result.getResponseBody();
//                    assertNotNull(getCartResponse);
//                    assertNotNull(getCartResponse.getCheckoutRedirectUrl());
//                    assertEquals(getCartResponse.getCheckoutRedirectUrl(), "testUrl");
//                });

    }

    @Test
    void getMdbShouldReturnErrorUrlOnKoRequest() throws Exception {
        when(mbdService.getMdb(any())).thenAnswer(item ->
                Mono.error(new RuntimeException("")));
        webClient.post().uri("/mbd").bodyValue(
                        objectMapper.writeValueAsBytes(GetMdbRequest.builder()
                                .idCiService("test")
                                .paymentNotices(Collections.singletonList(
                                        PaymentNotice.builder().build()
                                ))
                                .returnUrls(ReturnUrls.builder().errorUrl("test").build())
                                .build())).header("Content-Type",MediaType.APPLICATION_JSON_VALUE)
                .exchange().expectStatus().is5xxServerError()
                .expectBody(GetCartResponse.class)
                .consumeWith(result -> {
                    GetCartResponse getCartResponse = result.getResponseBody();
                    assertNotNull(getCartResponse);
                    assertNotNull(getCartResponse.getCheckoutRedirectUrl());
                    assertEquals(getCartResponse.getCheckoutRedirectUrl(), "testUrl");
                });
    }

    @Test
    void getPaymentReceipts() throws Exception {
        when(mbdService.getPaymentReceipts(any(),any())).thenAnswer(item ->
                Mono.just(ServerResponse.ok().bodyValue("".getBytes())));
//        mvc.perform(get("/mbd-payments/test/receipt/30000000001")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().is2xxSuccessful());
    }

}