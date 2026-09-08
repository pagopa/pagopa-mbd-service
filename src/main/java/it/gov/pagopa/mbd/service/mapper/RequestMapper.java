package it.gov.pagopa.mbd.service.mapper;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.hibernate.validator.internal.util.Contracts.assertTrue;

import it.gov.pagopa.mbd.service.exception.CartMappingException;
import it.gov.pagopa.mbd.service.model.carts.CartPaymentNotice;
import it.gov.pagopa.mbd.service.model.carts.GetCartRequest;
import it.gov.pagopa.mbd.service.model.carts.ReturnUrls;
import it.gov.pagopa.mbd.service.model.mdb.GetMbdRequest;
import it.gov.pagopa.mbd.service.model.mdb.PaymentNotice;
import it.gov.pagopa.mbd.service.model.xml.node.marcaDaBollo.CtService;
import it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp.CtPaymentOptionDescription;
import it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp.CtPaymentOptionsDescriptionList;
import it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp.DemandPaymentNoticeRequest;
import it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp.DemandPaymentNoticeResponse;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

public class RequestMapper {

  public static DemandPaymentNoticeRequest mapDemandPaymentNoticeRequest(
      String idPsp,
      String idBrokerPsp,
      String idChannel,
      String fiscalCodeEC,
      Jaxb2Marshaller marshaller,
      GetMbdRequest getMdbRequest) {

    PaymentNotice paymentNotice = getMdbRequest.getPaymentNotices().get(0);

    CtService ctMarcaDaBollo =
        CtService.builder()
            .debtorFiscalCode(paymentNotice.getFiscalCode())
            .debtorName(paymentNotice.getFirstName())
            .debtorSurname(paymentNotice.getLastName())
            .debtorEmail(paymentNotice.getEmail())
            .debtorProvince(paymentNotice.getProvince())
            .ciFiscalCode(fiscalCodeEC)
            .documentHash(paymentNotice.getDocumentHash())
            .build();

    return DemandPaymentNoticeRequest.builder()
        .idPSP(idPsp)
        .idBrokerPSP(idBrokerPsp)
        .idChannel(idChannel)
        .idSoggettoServizio(getMdbRequest.getIdCIService())
        .password("")
        .datiSpecificiServizio(
            Base64.getMimeEncoder()
                .encode(
                    ("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                         + "<service xmlns=\"http://PuntoAccessoPSP.spcoop.gov.it/GeneralService\""
                         + " xsi:schemaLocation=\"http://PuntoAccessoPSP.spcoop.gov.it/GeneralService"
                         + " schema.xsd\""
                         + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
                         + "  <amount>"
                            + getMdbRequest.getPaymentNotices().get(0).getAmount()
                            + "</amount>\n"
                            + "  <debtorFiscalCode>"
                            + ctMarcaDaBollo.getDebtorFiscalCode()
                            + "</debtorFiscalCode>\n"
                            + "  <debtorName>"
                            + ctMarcaDaBollo.getDebtorName()
                            + "</debtorName>\n"
                            + "  <debtorSurname>"
                            + ctMarcaDaBollo.getDebtorSurname()
                            + "</debtorSurname>\n"
                            + "  <debtorEmail>"
                            + ctMarcaDaBollo.getDebtorEmail()
                            + "</debtorEmail>\n"
                            + "  <debtorProvince>"
                            + ctMarcaDaBollo.getDebtorProvince()
                            + "</debtorProvince>\n"
                            + "  <ciFiscalCode>"
                            + ctMarcaDaBollo.getCiFiscalCode()
                            + "</ciFiscalCode>\n"
                            + "  <documentHash>"
                            + ctMarcaDaBollo.getDocumentHash()
                            + "</documentHash>\n"
                            + "</service>")
                        .getBytes()))
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
                  .returnCancelUrl(request.getReturnUrls().getWaitingUrl())
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

  private RequestMapper() {
  }
}
