package it.gov.pagopa.mbd.service.mapper;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.hibernate.validator.internal.util.Contracts.assertTrue;

import it.gov.pagopa.mbd.service.exception.CartMappingException;
import it.gov.pagopa.mbd.service.model.carts.CartPaymentNotice;
import it.gov.pagopa.mbd.service.model.carts.GetCartRequest;
import it.gov.pagopa.mbd.service.model.carts.GetCartRequestV2;
import it.gov.pagopa.mbd.service.model.carts.ReturnUrls;
import it.gov.pagopa.mbd.service.model.carts.ReturnUrlsV2;
import it.gov.pagopa.mbd.service.model.mdb.Debtor;
import it.gov.pagopa.mbd.service.model.mdb.GetMbdRequest;
import it.gov.pagopa.mbd.service.model.mdb.GetMbdRequestV2;
import it.gov.pagopa.mbd.service.model.mdb.PaymentNoticeV2;
import it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp.CtPaymentOptionDescription;
import it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp.CtPaymentOptionsDescriptionList;
import it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp.DemandPaymentNoticeRequest;
import it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp.DemandPaymentNoticeResponse;
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
      String fiscalCodeEC,
      GetMbdRequestV2 getMdbRequest) {

    PaymentNoticeV2 paymentNotice = getMdbRequest.getPaymentNotices().get(0);
    Debtor debtor = paymentNotice.getDebtor();
    String formattedServiceData =
        String.format(
            DEMAND_PAYMENT_SERVICE_DATA,
            paymentNotice.getAmount(),
            debtor.getUniqueIdentifier().getType(),
            debtor.getUniqueIdentifier().getValue(),
            debtor.getFullName(),
            debtor.getEmail(),
            fiscalCodeEC,
            paymentNotice.getProvince(),
            paymentNotice.getDocumentHash());

    return DemandPaymentNoticeRequest.builder()
        .idPSP(idPsp)
        .idBrokerPSP(idBrokerPsp)
        .idChannel(idChannel)
        .idSoggettoServizio(getMdbRequest.getIdCIService())
        .password("")
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
              ReturnUrls.builder()
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
              ReturnUrlsV2.builder()
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

  private RequestMapper() {}
}
