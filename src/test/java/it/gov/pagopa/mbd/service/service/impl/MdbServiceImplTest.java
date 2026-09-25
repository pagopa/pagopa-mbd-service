package it.gov.pagopa.mbd.service.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.gov.pagopa.mbd.service.client.ReactiveClient;
import it.gov.pagopa.mbd.service.exception.AppError;
import it.gov.pagopa.mbd.service.exception.AppException;
import it.gov.pagopa.mbd.service.exception.WebClientException;
import it.gov.pagopa.mbd.service.model.carts.GetCartResponse;
import it.gov.pagopa.mbd.service.model.mbd.CreateMbdRequestV2;
import it.gov.pagopa.mbd.service.model.mbd.Debtor;
import it.gov.pagopa.mbd.service.model.mbd.GetMbdRequest;
import it.gov.pagopa.mbd.service.model.mbd.GetMdbReceipt;
import it.gov.pagopa.mbd.service.model.mbd.PaymentNotice;
import it.gov.pagopa.mbd.service.model.mbd.PaymentNoticeV2;
import it.gov.pagopa.mbd.service.model.mbd.ReturnUrls;
import it.gov.pagopa.mbd.service.model.mbd.ReturnUrlsV2;
import it.gov.pagopa.mbd.service.model.mbd.UniqueIdentifier;
import it.gov.pagopa.mbd.service.service.MbdService;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.CtPaymentOptionDescription;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.CtPaymentOptionsDescriptionList;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.CtQrCode;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.DemandPaymentNoticeResponse;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.StAmountOptionPSP;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.StOutcome;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import java.math.BigDecimal;
import java.util.Collections;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@SpringBootTest
class MdbServiceImplTest {

  public static final String FISCAL_CODE_EC = "organization_fiscal_code";
  public static final String NAV = "3000000001";
  public static final String FISCAL_CODE = "JHNDOE00A01B157N";

  @MockBean private ReactiveClient reactiveClient;

  @Inject private MbdService mbdService;

  @Test
  void createMbd_OK() throws DatatypeConfigurationException {
    DemandPaymentNoticeResponse demandPaymentNoticeResponse = buildDemandResponse();
    when(reactiveClient.demandPaymentNotice(any()))
        .thenAnswer(item -> Mono.just(demandPaymentNoticeResponse));

    GetCartResponse getCartResponse =
        GetCartResponse.builder().checkoutRedirectUrl("testUrl").build();
    when(reactiveClient.getCart(any())).thenAnswer(item -> Mono.just(getCartResponse));

    GetMbdRequest getMbdRequest = buildGetMbdRequest(true);
    Mono<GetCartResponse> responseMono =
        assertDoesNotThrow(() -> mbdService.createMbd(FISCAL_CODE_EC, getMbdRequest));

    GetCartResponse response = responseMono.block();
    assertNotNull(response);
    assertEquals("testUrl", response.getCheckoutRedirectUrl());
  }

  @Test
  void createMbd_KO_ClientErrorOnDemand() {
    when(reactiveClient.demandPaymentNotice(any()))
        .thenAnswer(item -> Mono.error(new WebClientException("Error", null)));

    GetMbdRequest getMbdRequest = buildGetMbdRequest(true);
    Mono<GetCartResponse> responseMono =
        assertDoesNotThrow(() -> mbdService.createMbd(FISCAL_CODE_EC, getMbdRequest));
    AppException appException = assertThrows(AppException.class, responseMono::block);
    assertNotNull(appException);
    assertEquals(AppError.PAYMENT_NOTICE_REQUEST_CALL_ERROR.title, appException.getTitle());

    verify(reactiveClient, never()).getCart(any());
  }

  @Test
  void createMbd_KO_ErrorMappingDemandResponseToCartRequest() {
    DemandPaymentNoticeResponse demandPaymentNoticeResponse = buildInvalidDemandResponse();
    when(reactiveClient.demandPaymentNotice(any()))
        .thenAnswer(item -> Mono.just(demandPaymentNoticeResponse));

    GetMbdRequest getMbdRequest = buildGetMbdRequest(true);
    Mono<GetCartResponse> responseMono =
        assertDoesNotThrow(() -> mbdService.createMbd(FISCAL_CODE_EC, getMbdRequest));
    AppException appException = assertThrows(AppException.class, responseMono::block);
    assertNotNull(appException);
    assertEquals(AppError.CART_REQUEST_MAP_ERROR.title, appException.getTitle());

    verify(reactiveClient, never()).getCart(any());
  }

  @Test
  void createMbd_KO_ClientErrorOnGetCart() throws DatatypeConfigurationException {
    DemandPaymentNoticeResponse demandPaymentNoticeResponse = buildDemandResponse();
    when(reactiveClient.demandPaymentNotice(any()))
        .thenAnswer(item -> Mono.just(demandPaymentNoticeResponse));

    when(reactiveClient.getCart(any()))
        .thenAnswer(item -> Mono.error(new WebClientException("Error", null)));

    GetMbdRequest getMbdRequest = buildGetMbdRequest(true);
    Mono<GetCartResponse> responseMono =
        assertDoesNotThrow(() -> mbdService.createMbd(FISCAL_CODE_EC, getMbdRequest));
    AppException appException = assertThrows(AppException.class, responseMono::block);
    assertNotNull(appException);
    assertEquals(AppError.CART_REQUEST_CALL_ERROR.title, appException.getTitle());
  }

  @Test
  void createMbd_KO_RequestValidationError_WrongDocumentHash()
      throws DatatypeConfigurationException {
    DemandPaymentNoticeResponse demandPaymentNoticeResponse = buildDemandResponse();
    when(reactiveClient.demandPaymentNotice(any()))
        .thenAnswer(item -> Mono.just(demandPaymentNoticeResponse));

    GetCartResponse getCartResponse =
        GetCartResponse.builder().checkoutRedirectUrl("testUrl").build();
    when(reactiveClient.getCart(any())).thenAnswer(item -> Mono.just(getCartResponse));

    GetMbdRequest getMbdRequest = buildGetMbdRequest(false);
    Mono<GetCartResponse> responseMono = mbdService.createMbd(FISCAL_CODE_EC, getMbdRequest);
    assertThrows(ConstraintViolationException.class, responseMono::block);
  }

  @Test
  void createMbd_KO_RequestValidationError_InvalidFiscalCode() throws DatatypeConfigurationException {
    DemandPaymentNoticeResponse demandPaymentNoticeResponse = buildDemandResponse();
    when(reactiveClient.demandPaymentNotice(any()))
        .thenAnswer(item -> Mono.just(demandPaymentNoticeResponse));

    GetCartResponse getCartResponse =
        GetCartResponse.builder().checkoutRedirectUrl("testUrl").build();
    when(reactiveClient.getCart(any())).thenAnswer(item -> Mono.just(getCartResponse));

    GetMbdRequest getMbdRequest = buildGetMbdRequest(true);
    getMbdRequest.getPaymentNotices().get(0).setFiscalCode("AAAAAAA");
    Mono<GetCartResponse> responseMono = mbdService.createMbd(FISCAL_CODE_EC, getMbdRequest);
    assertThrows(ConstraintViolationException.class, responseMono::block);
  }

  @Test
  void createMbdV2_OK() throws DatatypeConfigurationException {
    DemandPaymentNoticeResponse demandPaymentNoticeResponse = buildDemandResponse();
    when(reactiveClient.demandPaymentNotice(any()))
        .thenAnswer(item -> Mono.just(demandPaymentNoticeResponse));

    GetCartResponse getCartResponse =
        GetCartResponse.builder().checkoutRedirectUrl("testUrl").build();
    when(reactiveClient.getCartV2(any())).thenAnswer(item -> Mono.just(getCartResponse));

    CreateMbdRequestV2 getMbdRequest = buildGetMbdRequestV2(true);
    Mono<GetCartResponse> responseMono =
        assertDoesNotThrow(() -> mbdService.createMbdV2(FISCAL_CODE_EC, getMbdRequest));

    GetCartResponse response = responseMono.block();
    assertNotNull(response);
    assertEquals("testUrl", response.getCheckoutRedirectUrl());
  }

  @Test
  void createMbdV2_KO_ClientErrorOnDemand() {
    when(reactiveClient.demandPaymentNotice(any()))
        .thenAnswer(item -> Mono.error(new WebClientException("Error", null)));

    CreateMbdRequestV2 getMbdRequest = buildGetMbdRequestV2(true);
    Mono<GetCartResponse> responseMono =
        assertDoesNotThrow(() -> mbdService.createMbdV2(FISCAL_CODE_EC, getMbdRequest));
    AppException appException = assertThrows(AppException.class, responseMono::block);
    assertNotNull(appException);
    assertEquals(AppError.PAYMENT_NOTICE_REQUEST_CALL_ERROR.title, appException.getTitle());

    verify(reactiveClient, never()).getCartV2(any());
  }

  @Test
  void createMbdV2_KO_ErrorMappingDemandResponseToCartRequest() {
    DemandPaymentNoticeResponse demandPaymentNoticeResponse = buildInvalidDemandResponse();
    when(reactiveClient.demandPaymentNotice(any()))
        .thenAnswer(item -> Mono.just(demandPaymentNoticeResponse));

    CreateMbdRequestV2 getMbdRequest = buildGetMbdRequestV2(true);
    Mono<GetCartResponse> responseMono =
        assertDoesNotThrow(() -> mbdService.createMbdV2(FISCAL_CODE_EC, getMbdRequest));
    AppException appException = assertThrows(AppException.class, responseMono::block);
    assertNotNull(appException);
    assertEquals(AppError.CART_REQUEST_MAP_ERROR.title, appException.getTitle());

    verify(reactiveClient, never()).getCartV2(any());
  }

  @Test
  void createMbdV2_KO_ClientErrorOnGetCart() throws DatatypeConfigurationException {
    DemandPaymentNoticeResponse demandPaymentNoticeResponse = buildDemandResponse();
    when(reactiveClient.demandPaymentNotice(any()))
        .thenAnswer(item -> Mono.just(demandPaymentNoticeResponse));

    when(reactiveClient.getCartV2(any()))
        .thenAnswer(item -> Mono.error(new WebClientException("Error", null)));

    CreateMbdRequestV2 getMbdRequest = buildGetMbdRequestV2(true);
    Mono<GetCartResponse> responseMono =
        assertDoesNotThrow(() -> mbdService.createMbdV2(FISCAL_CODE_EC, getMbdRequest));
    AppException appException = assertThrows(AppException.class, responseMono::block);
    assertNotNull(appException);
    assertEquals(AppError.CART_REQUEST_CALL_ERROR.title, appException.getTitle());
  }

  @Test
  void createMbdV2_KO_RequestValidationError_WrongDocumentHash()
      throws DatatypeConfigurationException {
    DemandPaymentNoticeResponse demandPaymentNoticeResponse = buildDemandResponse();
    when(reactiveClient.demandPaymentNotice(any()))
        .thenAnswer(item -> Mono.just(demandPaymentNoticeResponse));

    GetCartResponse getCartResponse =
        GetCartResponse.builder().checkoutRedirectUrl("testUrl").build();
    when(reactiveClient.getCartV2(any())).thenAnswer(item -> Mono.just(getCartResponse));

    CreateMbdRequestV2 getMbdRequest = buildGetMbdRequestV2(false);
    Mono<GetCartResponse> responseMono = mbdService.createMbdV2(FISCAL_CODE_EC, getMbdRequest);
    assertThrows(ConstraintViolationException.class, responseMono::block);
  }

  @Test
  void createMbdV2_KO_RequestValidationError_InvalidFiscalCode()
      throws DatatypeConfigurationException {
    DemandPaymentNoticeResponse demandPaymentNoticeResponse = buildDemandResponse();
    when(reactiveClient.demandPaymentNotice(any()))
        .thenAnswer(item -> Mono.just(demandPaymentNoticeResponse));

    GetCartResponse getCartResponse =
        GetCartResponse.builder().checkoutRedirectUrl("testUrl").build();
    when(reactiveClient.getCartV2(any())).thenAnswer(item -> Mono.just(getCartResponse));

    CreateMbdRequestV2 getMbdRequest = buildGetMbdRequestV2(true);
    getMbdRequest.getPaymentNotices().get(0).getDebtor().getUniqueIdentifier().setValue("AAAAAAA");
    Mono<GetCartResponse> responseMono = mbdService.createMbdV2(FISCAL_CODE_EC, getMbdRequest);
    assertThrows(ConstraintViolationException.class, responseMono::block);
  }

  @Test
  void getPaymentReceipts_OK() {
    when(reactiveClient.getPaymentReceipt(any(), any()))
        .thenAnswer(item -> Mono.just(FISCAL_CODE_EC.getBytes()));

    Mono<GetMdbReceipt> responseMono =
        assertDoesNotThrow(() -> mbdService.getPaymentReceipts(FISCAL_CODE_EC, NAV));

    GetMdbReceipt response = responseMono.block();
    assertNotNull(response);
    assertNotNull(response.getContent());
  }

  @Test
  void getPaymentReceipts_KO_ErrorRetrievingReceipt_IllegalArgumentException() {
    when(reactiveClient.getPaymentReceipt(any(), any()))
        .thenAnswer(item -> Mono.error(new IllegalArgumentException()));

    Mono<GetMdbReceipt> responseMono =
        assertDoesNotThrow(() -> mbdService.getPaymentReceipts(FISCAL_CODE_EC, NAV));
    AppException e = assertThrows(AppException.class, responseMono::block);

    assertNotNull(e);
    assertEquals(AppError.PAYMENT_RECEIPTS_RESPONSE_MAPPING_ERROR.title, e.getTitle());
  }

  @Test
  void getPaymentReceipts_KO_ErrorRetrievingReceipt_404() {
    WebClientResponseException error =
        new WebClientResponseException(404, "Not Found", null, null, null);
    when(reactiveClient.getPaymentReceipt(any(), any())).thenAnswer(item -> Mono.error(error));

    Mono<GetMdbReceipt> responseMono =
        assertDoesNotThrow(() -> mbdService.getPaymentReceipts(FISCAL_CODE_EC, NAV));
    AppException e = assertThrows(AppException.class, responseMono::block);

    assertNotNull(e);
    assertEquals(AppError.PAYMENT_RECEIPTS_NOT_FOUND.title, e.getTitle());
  }

  @Test
  void getPaymentReceipts_KO_ErrorRetrievingReceipt_5XX() {
    WebClientResponseException error =
        new WebClientResponseException(500, "Error", null, null, null);
    when(reactiveClient.getPaymentReceipt(any(), any())).thenAnswer(item -> Mono.error(error));

    Mono<GetMdbReceipt> responseMono =
        assertDoesNotThrow(() -> mbdService.getPaymentReceipts(FISCAL_CODE_EC, NAV));
    AppException e = assertThrows(AppException.class, responseMono::block);

    assertNotNull(e);
    assertEquals(AppError.PAYMENT_RECEIPTS_CALL_ERROR.title, e.getTitle());
  }

  private GetMbdRequest buildGetMbdRequest(boolean validDocumentHash) {
    return GetMbdRequest.builder()
        .idCIService("1000")
        .paymentNotices(
            Collections.singletonList(
                PaymentNotice.builder()
                    .amount(1000L)
                    .documentHash(validDocumentHash ? "1" : null)
                    .email("test@gmail.com")
                    .fiscalCode(FISCAL_CODE)
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

  private CreateMbdRequestV2 buildGetMbdRequestV2(boolean validDocumentHash) {
    return CreateMbdRequestV2.builder()
        .paymentNotices(
            Collections.singletonList(
                PaymentNoticeV2.builder()
                    .amount(1000L)
                    .province("RM")
                    .documentHash(validDocumentHash ? "1" : null)
                    .debtor(
                        Debtor.builder()
                            .email("test@gmail.com")
                            .fullName("debtor first name debtor last name")
                            .uniqueIdentifier(
                                UniqueIdentifier.builder()
                                    .type(UniqueIdentifier.UniqueIdentifierType.F)
                                    .value(FISCAL_CODE)
                                    .build())
                            .build())
                    .build()))
        .returnUrls(
            ReturnUrlsV2.builder()
                .errorUrl("testUrl")
                .successUrl("testUrl")
                .cancelUrl("testUrl")
                .waitingUrl("testUrl")
                .build())
        .build();
  }

  private DemandPaymentNoticeResponse buildDemandResponse() throws DatatypeConfigurationException {
    CtPaymentOptionDescription paymentOptionDescription = new CtPaymentOptionDescription();
    paymentOptionDescription.setPaymentNote("Note");
    paymentOptionDescription.setAmount(BigDecimal.TEN);
    paymentOptionDescription.setDueDate(DatatypeFactory.newInstance().newXMLGregorianCalendar());
    paymentOptionDescription.setOptions(StAmountOptionPSP.ANY);

    CtPaymentOptionsDescriptionList paymentOptionsDescriptionList =
        new CtPaymentOptionsDescriptionList();
    paymentOptionsDescriptionList.getPaymentOptionDescription().add(paymentOptionDescription);

    CtQrCode ctQrCode = new CtQrCode();
    ctQrCode.setFiscalCode(FISCAL_CODE);
    ctQrCode.setNoticeNumber(NAV);

    DemandPaymentNoticeResponse demandPaymentNoticeResponse = new DemandPaymentNoticeResponse();
    demandPaymentNoticeResponse.setQrCode(ctQrCode);
    demandPaymentNoticeResponse.setPaymentList(paymentOptionsDescriptionList);
    demandPaymentNoticeResponse.setOutcome(StOutcome.OK);

    return demandPaymentNoticeResponse;
  }

  private DemandPaymentNoticeResponse buildInvalidDemandResponse() {
    CtQrCode ctQrCode = new CtQrCode();
    ctQrCode.setFiscalCode(FISCAL_CODE);
    ctQrCode.setNoticeNumber(NAV);

    DemandPaymentNoticeResponse demandPaymentNoticeResponse = new DemandPaymentNoticeResponse();
    demandPaymentNoticeResponse.setQrCode(ctQrCode);
    demandPaymentNoticeResponse.setOutcome(StOutcome.KO);

    return demandPaymentNoticeResponse;
  }
}
