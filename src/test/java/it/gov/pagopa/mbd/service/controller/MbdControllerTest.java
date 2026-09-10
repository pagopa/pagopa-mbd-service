package it.gov.pagopa.mbd.service.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.mbd.service.exception.AppError;
import it.gov.pagopa.mbd.service.exception.AppException;
import it.gov.pagopa.mbd.service.model.ProblemJson;
import it.gov.pagopa.mbd.service.model.carts.GetCartErrorResponse;
import it.gov.pagopa.mbd.service.model.carts.GetCartResponse;
import it.gov.pagopa.mbd.service.model.mdb.GetMbdRequest;
import it.gov.pagopa.mbd.service.model.mdb.GetMbdRequestV2;
import it.gov.pagopa.mbd.service.model.mdb.GetMdbReceipt;
import it.gov.pagopa.mbd.service.model.mdb.PaymentNotice;
import it.gov.pagopa.mbd.service.model.mdb.PaymentNoticeV2;
import it.gov.pagopa.mbd.service.model.mdb.ReturnUrls;
import it.gov.pagopa.mbd.service.model.mdb.ReturnUrlsV2;
import it.gov.pagopa.mbd.service.service.MbdService;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = MbdController.class)
class MbdControllerTest {

  public static final byte[] MBD_BYTES = "ABC".getBytes();
  public static final String TEST_URL = "testUrl";

  @MockBean private MbdService mbdService;

  @Autowired private WebTestClient webClient;

  @BeforeEach
  void setUp() {
    Mockito.reset(mbdService);
  }

  @Inject ObjectMapper objectMapper;

  @Test
  void getMdb_OK_ShouldReturnCheckoutUrl() throws Exception {
    when(mbdService.getMbd(any(), any()))
        .thenAnswer(
            item -> Mono.just(GetCartResponse.builder().checkoutRedirectUrl(TEST_URL).build()));
    webClient
        .post()
        .uri("/v1/organizations/test/mbd")
        .bodyValue(
            objectMapper.writeValueAsBytes(
                GetMbdRequestV2.builder()
                    .idCIService("test")
                    .paymentNotices(Collections.singletonList(PaymentNoticeV2.builder().build()))
                    .returnUrls(ReturnUrlsV2.builder().build())
                    .build()))
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(GetCartResponse.class)
        .consumeWith(
            result -> {
              GetCartResponse getCartResponse = result.getResponseBody();
              assertNotNull(getCartResponse);
              assertNotNull(getCartResponse.getCheckoutRedirectUrl());
              assertEquals(TEST_URL, getCartResponse.getCheckoutRedirectUrl());
            });
  }

  @Test
  void getMdb_KO_ShouldReturnErrorUrl_GenericException() throws Exception {
    when(mbdService.getMbd(any(), any())).thenAnswer(item -> Mono.error(new RuntimeException("")));
    webClient
        .post()
        .uri("/v1/organizations/test/mbd")
        .bodyValue(
            objectMapper.writeValueAsBytes(
                GetMbdRequest.builder()
                    .idCIService("test")
                    .paymentNotices(Collections.singletonList(PaymentNotice.builder().build()))
                    .returnUrls(ReturnUrls.builder().errorUrl(TEST_URL).build())
                    .build()))
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus()
        .is5xxServerError()
        .expectBody(GetCartErrorResponse.class)
        .consumeWith(
            result -> {
              GetCartErrorResponse getCartResponse = result.getResponseBody();
              assertNotNull(getCartResponse);
              assertNotNull(getCartResponse.getErrorUrl());
              assertEquals(TEST_URL, getCartResponse.getErrorUrl());
            });
  }

  @Test
  void getMdb_KO_ShouldReturnErrorUrl_ConstraintViolationException() throws Exception {
    when(mbdService.getMbd(any(), any()))
        .thenAnswer(item -> Mono.error(new ConstraintViolationException(Collections.emptySet())));
    webClient
        .post()
        .uri("/v1/organizations/test/mbd")
        .bodyValue(
            objectMapper.writeValueAsBytes(
                GetMbdRequest.builder()
                    .idCIService("test")
                    .paymentNotices(Collections.singletonList(PaymentNotice.builder().build()))
                    .returnUrls(ReturnUrls.builder().errorUrl(TEST_URL).build())
                    .build()))
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus()
        .is4xxClientError()
        .expectBody(ProblemJson.class)
        .consumeWith(
            result -> {
              ProblemJson problemJson = result.getResponseBody();
              assertNotNull(problemJson);
              assertEquals(AppError.BAD_REQUEST.title, problemJson.getTitle());
            });
  }

  @Test
  void getMdb_KO_ShouldReturnErrorUrl_AppException() throws Exception {
    when(mbdService.getMbd(any(), any()))
        .thenAnswer(item -> Mono.error(new AppException(AppError.CART_REQUEST_CALL_ERROR)));
    webClient
        .post()
        .uri("/v1/organizations/test/mbd")
        .bodyValue(
            objectMapper.writeValueAsBytes(
                GetMbdRequest.builder()
                    .idCIService("test")
                    .paymentNotices(Collections.singletonList(PaymentNotice.builder().build()))
                    .returnUrls(ReturnUrls.builder().errorUrl(TEST_URL).build())
                    .build()))
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus()
        .is5xxServerError()
        .expectBody(GetCartErrorResponse.class)
        .consumeWith(
            result -> {
              GetCartErrorResponse getCartResponse = result.getResponseBody();
              assertNotNull(getCartResponse);
              assertNotNull(getCartResponse.getErrorUrl());
              assertEquals(TEST_URL, getCartResponse.getErrorUrl());
            });
  }

  @Test
  void getMdbV2_OK_ShouldReturnCheckoutUrl() throws Exception {
    when(mbdService.getMbdV2(any(), any()))
        .thenAnswer(
            item -> Mono.just(GetCartResponse.builder().checkoutRedirectUrl(TEST_URL).build()));
    webClient
        .post()
        .uri("/v2/organizations/test/mbd")
        .bodyValue(
            objectMapper.writeValueAsBytes(
                GetMbdRequestV2.builder()
                    .idCIService("test")
                    .paymentNotices(Collections.singletonList(PaymentNoticeV2.builder().build()))
                    .returnUrls(ReturnUrlsV2.builder().build())
                    .build()))
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(GetCartResponse.class)
        .consumeWith(
            result -> {
              GetCartResponse getCartResponse = result.getResponseBody();
              assertNotNull(getCartResponse);
              assertNotNull(getCartResponse.getCheckoutRedirectUrl());
              assertEquals(TEST_URL, getCartResponse.getCheckoutRedirectUrl());
            });
  }

  @Test
  void getMdbV2_KO_ShouldReturnErrorUrl_GenericException() throws Exception {
    when(mbdService.getMbdV2(any(), any()))
        .thenAnswer(item -> Mono.error(new RuntimeException("")));
    webClient
        .post()
        .uri("/v2/organizations/test/mbd")
        .bodyValue(
            objectMapper.writeValueAsBytes(
                GetMbdRequestV2.builder()
                    .idCIService("test")
                    .paymentNotices(Collections.singletonList(PaymentNoticeV2.builder().build()))
                    .returnUrls(ReturnUrlsV2.builder().errorUrl(TEST_URL).build())
                    .build()))
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus()
        .is5xxServerError()
        .expectBody(GetCartErrorResponse.class)
        .consumeWith(
            result -> {
              GetCartErrorResponse getCartResponse = result.getResponseBody();
              assertNotNull(getCartResponse);
              assertNotNull(getCartResponse.getErrorUrl());
              assertEquals(TEST_URL, getCartResponse.getErrorUrl());
            });
  }

  @Test
  void getMdbV2_KO_ShouldReturnErrorUrl_ConstraintViolationException() throws Exception {
    when(mbdService.getMbdV2(any(), any()))
        .thenAnswer(item -> Mono.error(new ConstraintViolationException(Collections.emptySet())));
    webClient
        .post()
        .uri("/v2/organizations/test/mbd")
        .bodyValue(
            objectMapper.writeValueAsBytes(
                GetMbdRequestV2.builder()
                    .idCIService("test")
                    .paymentNotices(Collections.singletonList(PaymentNoticeV2.builder().build()))
                    .returnUrls(ReturnUrlsV2.builder().errorUrl(TEST_URL).build())
                    .build()))
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus()
        .is4xxClientError()
        .expectBody(ProblemJson.class)
        .consumeWith(
            result -> {
              ProblemJson problemJson = result.getResponseBody();
              assertNotNull(problemJson);
              assertEquals(AppError.BAD_REQUEST.title, problemJson.getTitle());
            });
  }

  @Test
  void getMdbV2_KO_ShouldReturnErrorUrl_AppException() throws Exception {
    when(mbdService.getMbdV2(any(), any()))
        .thenAnswer(item -> Mono.error(new AppException(AppError.CART_REQUEST_CALL_ERROR)));
    webClient
        .post()
        .uri("/v2/organizations/test/mbd")
        .bodyValue(
            objectMapper.writeValueAsBytes(
                GetMbdRequestV2.builder()
                    .idCIService("test")
                    .paymentNotices(Collections.singletonList(PaymentNoticeV2.builder().build()))
                    .returnUrls(ReturnUrlsV2.builder().errorUrl(TEST_URL).build())
                    .build()))
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus()
        .is5xxServerError()
        .expectBody(GetCartErrorResponse.class)
        .consumeWith(
            result -> {
              GetCartErrorResponse getCartResponse = result.getResponseBody();
              assertNotNull(getCartResponse);
              assertNotNull(getCartResponse.getErrorUrl());
              assertEquals(TEST_URL, getCartResponse.getErrorUrl());
            });
  }

  @Test
  void getPaymentReceipts_OK_ShouldReturnContent() {
    when(mbdService.getPaymentReceipts(any(), any()))
        .thenAnswer(item -> Mono.just(GetMdbReceipt.builder().content(MBD_BYTES).build()));
    webClient
        .get()
        .uri("/v1/organizations/test/receipt/30000000001")
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(GetMdbReceipt.class)
        .consumeWith(
            result -> {
              GetMdbReceipt body = result.getResponseBody();
              assertNotNull(body);
              assertArrayEquals(MBD_BYTES, body.getContent());
            });
  }

  @Test
  void getPaymentReceiptsV2_OK_ShouldReturnContent() {
    when(mbdService.getPaymentReceipts(any(), any()))
        .thenAnswer(item -> Mono.just(GetMdbReceipt.builder().content(MBD_BYTES).build()));
    webClient
        .get()
        .uri("/v2/organizations/test/noticeNumbers/30000000001/mbd")
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(GetMdbReceipt.class)
        .consumeWith(
            result -> {
              GetMdbReceipt body = result.getResponseBody();
              assertNotNull(body);
              assertArrayEquals(MBD_BYTES, body.getContent());
            });
  }
}
