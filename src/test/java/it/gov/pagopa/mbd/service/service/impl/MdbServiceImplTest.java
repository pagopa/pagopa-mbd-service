package it.gov.pagopa.mbd.service.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import it.gov.pagopa.mbd.service.client.ReactiveClient;
import it.gov.pagopa.mbd.service.exception.AppException;
import it.gov.pagopa.mbd.service.exception.WebClientException;
import it.gov.pagopa.mbd.service.model.carts.GetCartResponse;
import it.gov.pagopa.mbd.service.model.mdb.GetMbdRequest;
import it.gov.pagopa.mbd.service.model.mdb.PaymentNotice;
import it.gov.pagopa.mbd.service.model.mdb.ReturnUrls;
import it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp.*;
import it.gov.pagopa.mbd.service.model.xml.xsd.common_types.v1_0.StOutcome;
import it.gov.pagopa.mbd.service.service.MbdService;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import java.math.BigDecimal;
import java.util.Collections;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

@SpringBootTest
class MdbServiceImplTest {

  public static final String FISCAL_CODE_EC = "organization_fiscal_code";
  public static final String NAV = "3000000001";

  @MockBean private ReactiveClient reactiveClient;

  @Inject private MbdService mbdService;

  @Test
  void getMdbShouldReturnResponseEntityOnValidData() throws DatatypeConfigurationException {
    DemandPaymentNoticeResponse demandPaymentNoticeResponse = buildDemandResponse();
    demandPaymentNoticeResponse.setOutcome(StOutcome.OK);
    when(reactiveClient.demandPaymentNotice(any()))
        .thenAnswer(item -> Mono.just(demandPaymentNoticeResponse));

    GetCartResponse getCartResponse =
        GetCartResponse.builder().checkoutRedirectUrl("testUrl").build();
    when(reactiveClient.getCart(any())).thenAnswer(item -> Mono.just(getCartResponse));

    GetMbdRequest getMbdRequest = buildGetMbdRequest(44);
    Mono<ResponseEntity> responseEntityMono =
        assertDoesNotThrow(() -> mbdService.getMbd(FISCAL_CODE_EC, getMbdRequest));

    ResponseEntity<GetCartResponse> responseEntity = responseEntityMono.block();
    assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
    GetCartResponse response = responseEntity.getBody();
    assertEquals("testUrl", response.getCheckoutRedirectUrl());
  }

  @Test
  void getMdbShouldReturnKoOnErrorClientCall() throws DatatypeConfigurationException {
    DemandPaymentNoticeResponse demandPaymentNoticeResponse = buildDemandResponse();
    demandPaymentNoticeResponse.setOutcome(StOutcome.OK);
    when(reactiveClient.demandPaymentNotice(any()))
        .thenAnswer(item -> Mono.just(demandPaymentNoticeResponse));

    when(reactiveClient.getCart(any()))
        .thenAnswer(item -> Mono.error(new WebClientException("Error", null)));

    GetMbdRequest getMbdRequest = buildGetMbdRequest(44);
    Mono<ResponseEntity> responseEntityMono =
        assertDoesNotThrow(() -> mbdService.getMbd(FISCAL_CODE_EC, getMbdRequest));
    assertThrows(AppException.class, responseEntityMono::block);
  }

  @Test
  void getMdbShouldReturnKOOnInvalidData_WrongDocumentHashLength()
      throws DatatypeConfigurationException {
    DemandPaymentNoticeResponse demandPaymentNoticeResponse = buildDemandResponse();
    demandPaymentNoticeResponse.setOutcome(StOutcome.OK);
    when(reactiveClient.demandPaymentNotice(any()))
        .thenAnswer(item -> Mono.just(demandPaymentNoticeResponse));

    GetCartResponse getCartResponse =
        GetCartResponse.builder().checkoutRedirectUrl("testUrl").build();
    when(reactiveClient.getCart(any())).thenAnswer(item -> Mono.just(getCartResponse));

    GetMbdRequest getMbdRequest = buildGetMbdRequest(10);
    Mono<ResponseEntity> responseEntityMono = mbdService.getMbd(FISCAL_CODE_EC, getMbdRequest);
    assertThrows(ConstraintViolationException.class, responseEntityMono::block);
  }

  @Test
  void getPaymentReceiptsShouldReturnOk() {
    when(reactiveClient.getPaymentReceipt(any(), any()))
        .thenAnswer(item -> Mono.just(FISCAL_CODE_EC.getBytes()));

    ResponseEntity<?> responseEntity = mbdService.getPaymentReceipts(FISCAL_CODE_EC, NAV).block();

    assertNotNull(responseEntity);
    assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
  }

  @Test
  void getPaymentReceiptsShouldReturnKoException() {
    WebClientException error = new WebClientException("Error on test call", null);
    when(reactiveClient.getPaymentReceipt(any(), any())).thenAnswer(item -> Mono.error(error));

    Mono<ResponseEntity> responseMono = mbdService.getPaymentReceipts(FISCAL_CODE_EC, NAV);
    assertThrows(AppException.class, responseMono::block);
  }

  private GetMbdRequest buildGetMbdRequest(int documentHashLength) {
    return GetMbdRequest.builder()
        .idCIService("1000")
        .paymentNotices(
            Collections.singletonList(
                PaymentNotice.builder()
                    .amount(1000L)
                    .documentHash("1".repeat(documentHashLength))
                    .email("test@gmail.com")
                    .fiscalCode("JHNDOE00A01B157N")
                    .lastName("debtor last name")
                    .firstName("debtor first name")
                    .province("RM")
                    .build()))
        .returnUrls(
            ReturnUrls.builder()
                .errorUrl("testUrl")
                .successUrl("testUrl")
                .cancelUrl("testUrl")
                .build())
        .build();
  }

  private DemandPaymentNoticeResponse buildDemandResponse() throws DatatypeConfigurationException {
    return DemandPaymentNoticeResponse.builder()
        .qrCode(CtQrCode.builder().noticeNumber(NAV).fiscalCode("JHNDOE00A01B157N").build())
        .paymentList(
            CtPaymentOptionsDescriptionList.builder()
                .paymentOptionDescription(
                    Collections.singletonList(
                        CtPaymentOptionDescription.builder()
                            .paymentNote("Note")
                            .amount(BigDecimal.TEN)
                            .dueDate(DatatypeFactory.newInstance().newXMLGregorianCalendar())
                            .options(StAmountOptionPSP.ANY)
                            .build()))
                .build())
        .build();
  }
}
