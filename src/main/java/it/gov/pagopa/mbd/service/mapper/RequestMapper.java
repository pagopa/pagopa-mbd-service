package it.gov.pagopa.mbd.service.mapper;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.hibernate.validator.internal.util.Contracts.assertTrue;

import it.gov.pagopa.mbd.service.exception.CartMappingException;
import it.gov.pagopa.mbd.service.model.carts.CartPaymentNotice;
import it.gov.pagopa.mbd.service.model.carts.CartReturnUrls;
import it.gov.pagopa.mbd.service.model.carts.CartReturnUrlsV2;
import it.gov.pagopa.mbd.service.model.carts.GetCartRequest;
import it.gov.pagopa.mbd.service.model.carts.GetCartRequestV2;
import it.gov.pagopa.mbd.service.model.mdb.Debtor;
import it.gov.pagopa.mbd.service.model.mdb.GetMbdRequest;
import it.gov.pagopa.mbd.service.model.mdb.GetMbdRequestV2;
import it.gov.pagopa.mbd.service.model.mdb.PaymentNotice;
import it.gov.pagopa.mbd.service.model.mdb.PaymentNoticeV2;
import it.gov.pagopa.mbd.service.model.mdb.ReturnUrlsV2;
import it.gov.pagopa.mbd.service.model.mdb.UniqueIdentifier;
import it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp.CtPaymentOptionDescription;
import it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp.CtPaymentOptionsDescriptionList;
import it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp.DemandPaymentNoticeRequest;
import it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp.DemandPaymentNoticeResponse;
import it.gov.pagopa.mbd.service.util.Constants;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

public class RequestMapper {

  private static final String DEMAND_PAYMENT_SERVICE_DATA =
"""
<?xml version="1.0" encoding="utf-8"?>
<service xmlns="http://PuntoAccessoPSP.spcoop.gov.it/GeneralService"
xsi:schemaLocation="http://PuntoAccessoPSP.spcoop.gov.it/GeneralService schema.xsd"
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <amount>%s</amount>
  <debtor>
    <uniqueIdentifier>
      <entityUniqueIdentifierType>%s</entityUniqueIdentifierType>
      <entityUniqueIdentifierValue>%s</entityUniqueIdentifierValue>
    </uniqueIdentifier>
    <fullName>%s</fullName>
    <email>%s</email>
   </debtor>
  <fiscalCode>%s</fiscalCode>
  <province>%s</province>
  <documentHash>%s</documentHash>
</service>
""";

  public static DemandPaymentNoticeRequest mapDemandPaymentNoticeRequest(
      String idPsp,
      String idBrokerPsp,
      String idChannel,
      String organizationFiscalCode,
      GetMbdRequestV2 getMdbRequest) {

    PaymentNoticeV2 paymentNotice = getMdbRequest.getPaymentNotices().get(0);
    Debtor debtor = paymentNotice.getDebtor();
    String formattedServiceData =
        String.format(
            DEMAND_PAYMENT_SERVICE_DATA,
            formatEuroCentAmount(paymentNotice.getAmount()),
            debtor.getUniqueIdentifier().getType(),
            debtor.getUniqueIdentifier().getValue(),
            debtor.getFullName(),
            debtor.getEmail(),
            organizationFiscalCode,
            paymentNotice.getProvince(),
            paymentNotice.getDocumentHash());

    return DemandPaymentNoticeRequest.builder()
        .idPSP(idPsp)
        .idBrokerPSP(idBrokerPsp)
        .idChannel(idChannel)
        .idSoggettoServizio(getMdbRequest.getIdCIService())
        .password("PLACEHOLDER")
        .datiSpecificiServizio(Base64.getMimeEncoder().encode(formattedServiceData.getBytes()))
        .build();
  }

  public static GetCartRequest mapCartRequest(
      GetMbdRequest request, DemandPaymentNoticeResponse demandPaymentNoticeResponse) {
    try {
      assertNotNull(demandPaymentNoticeResponse);
      CtPaymentOptionsDescriptionList ctPaymentOptionsDescriptionList =
          demandPaymentNoticeResponse.getPaymentList();
      assertNotNull(ctPaymentOptionsDescriptionList);
      List<CtPaymentOptionDescription> ctPaymentOptionsDescriptions =
          ctPaymentOptionsDescriptionList.getPaymentOptionDescription();
      assertNotNull(ctPaymentOptionsDescriptions);
      assertTrue(!ctPaymentOptionsDescriptions.isEmpty(), "Missing PaymentOption");
      assertNotNull(demandPaymentNoticeResponse.getQrCode());
      return GetCartRequest.builder()
          .emailNotice(request.getPaymentNotices().get(0).getEmail())
          .returnUrls(
              CartReturnUrls.builder()
                  .returnCancelUrl(request.getReturnUrls().getCancelUrl())
                  .returnErrorUrl(request.getReturnUrls().getErrorUrl())
                  .returnOkUrl(request.getReturnUrls().getSuccessUrl())
                  .build())
          .paymentNotices(
              Collections.singletonList(
                  CartPaymentNotice.builder()
                      .fiscalCode(demandPaymentNoticeResponse.getQrCode().getFiscalCode())
                      .amount(request.getPaymentNotices().get(0).getAmount())
                      .companyName(demandPaymentNoticeResponse.getCompanyName())
                      .description(demandPaymentNoticeResponse.getPaymentDescription())
                      .noticeNumber(demandPaymentNoticeResponse.getQrCode().getNoticeNumber())
                      .build()))
          .build();
    } catch (Exception e) {
      throw new CartMappingException(e.getMessage(), e);
    }
  }

  public static GetCartRequestV2 mapCartV2Request(
      GetMbdRequestV2 request, DemandPaymentNoticeResponse demandPaymentNoticeResponse) {
    try {
      assertNotNull(demandPaymentNoticeResponse);
      CtPaymentOptionsDescriptionList ctPaymentOptionsDescriptionList =
          demandPaymentNoticeResponse.getPaymentList();
      assertNotNull(ctPaymentOptionsDescriptionList);
      List<CtPaymentOptionDescription> ctPaymentOptionsDescriptions =
          ctPaymentOptionsDescriptionList.getPaymentOptionDescription();
      assertNotNull(ctPaymentOptionsDescriptions);
      assertTrue(!ctPaymentOptionsDescriptions.isEmpty(), "Missing PaymentOption");
      assertNotNull(demandPaymentNoticeResponse.getQrCode());

      return GetCartRequestV2.builder()
          .emailNotice(request.getPaymentNotices().get(0).getDebtor().getEmail())
          .returnUrls(
              CartReturnUrlsV2.builder()
                  .returnCancelUrl(request.getReturnUrls().getCancelUrl())
                  .returnErrorUrl(request.getReturnUrls().getErrorUrl())
                  .returnOkUrl(request.getReturnUrls().getSuccessUrl())
                  .returnWaitingUrl(request.getReturnUrls().getWaitingUrl())
                  .build())
          .paymentNotices(
              Collections.singletonList(
                  CartPaymentNotice.builder()
                      .fiscalCode(demandPaymentNoticeResponse.getQrCode().getFiscalCode())
                      .amount(request.getPaymentNotices().get(0).getAmount())
                      .companyName(demandPaymentNoticeResponse.getCompanyName())
                      .description(demandPaymentNoticeResponse.getPaymentDescription())
                      .noticeNumber(demandPaymentNoticeResponse.getQrCode().getNoticeNumber())
                      .build()))
          .build();
    } catch (Exception e) {
      throw new CartMappingException(e.getMessage(), e);
    }
  }

  public static GetMbdRequestV2 mapGetMbdRequestToGetMbdRequestV2(GetMbdRequest request) {
    return GetMbdRequestV2.builder()
        .idCIService(request.getIdCIService())
        .paymentNotices(
            request.getPaymentNotices().stream()
                .map(
                    paymentNotice ->
                        PaymentNoticeV2.builder()
                            .amount(paymentNotice.getAmount())
                            .province(paymentNotice.getProvince())
                            .documentHash(paymentNotice.getDocumentHash())
                            .debtor(buildDebtor(paymentNotice))
                            .build())
                .toList())
        .returnUrls(
            ReturnUrlsV2.builder()
                .cancelUrl(request.getReturnUrls().getCancelUrl())
                .errorUrl(request.getReturnUrls().getErrorUrl())
                .successUrl(request.getReturnUrls().getSuccessUrl())
                .build())
        .build();
  }

  private static Debtor buildDebtor(PaymentNotice paymentNotice) {
    return Debtor.builder()
        .fullName(paymentNotice.getFirstName() + " " + paymentNotice.getLastName())
        .uniqueIdentifier(
            UniqueIdentifier.builder()
                .type(extractUniqueIdentifierType(paymentNotice.getFiscalCode()))
                .value(paymentNotice.getFiscalCode())
                .build())
        .email(paymentNotice.getEmail())
        .build();
  }

  private static UniqueIdentifier.UniqueIdentifierType extractUniqueIdentifierType(
      String fiscalCode) {
    if (Constants.FISCAL_CODE_PATTERN.matcher(fiscalCode).matches()) {
      return UniqueIdentifier.UniqueIdentifierType.F;
    } else if (Constants.VAT_NUMBER_PATTERN.matcher(fiscalCode).matches()) {
      return UniqueIdentifier.UniqueIdentifierType.G;
    } else {
      throw new IllegalArgumentException("Invalid fiscal code or VAT number format");
    }
  }

  private static BigDecimal formatEuroCentAmount(long grandTotal) {
    BigDecimal amount = new BigDecimal(grandTotal);
    BigDecimal divider = new BigDecimal(100);
    return amount.divide(divider, 2, RoundingMode.UNNECESSARY);
  }

  private RequestMapper() {}
}
