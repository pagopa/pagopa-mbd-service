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
import it.gov.pagopa.mbd.service.util.Constants;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.CtPaymentOptionDescription;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.CtPaymentOptionsDescriptionList;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.DemandPaymentNoticeRequest;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.DemandPaymentNoticeResponse;
import it.gov.pagopa.pagopa_api.pa.marcadabollo.DebtorInfo;
import it.gov.pagopa.pagopa_api.pa.marcadabollo.ObjectFactory;
import it.gov.pagopa.pagopa_api.pa.marcadabollo.TipoMarcaDaBollo;
import it.gov.pagopa.pagopa_api.pa.pafornode.CtEntityUniqueIdentifier;
import it.gov.pagopa.pagopa_api.pa.pafornode.StEntityUniqueIdentifierType;
import jakarta.xml.bind.JAXBElement;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import javax.xml.transform.stream.StreamResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;

@Component
public class RequestMapper {

  private static final ObjectFactory objectFactory = new ObjectFactory();

  private final Jaxb2Marshaller jaxb2Marshaller;
  private final String mbdServiceId;

  public RequestMapper(Jaxb2Marshaller jaxb2Marshaller, @Value("${mbd.service.id}") String mbdServiceId) {
    this.jaxb2Marshaller = jaxb2Marshaller;
    this.mbdServiceId = mbdServiceId;

  }

  public DemandPaymentNoticeRequest mapDemandPaymentNoticeRequest(
      String idPsp,
      String idBrokerPsp,
      String idChannel,
      String organizationFiscalCode,
      GetMbdRequestV2 getMdbRequest) {

    PaymentNoticeV2 paymentNotice = getMdbRequest.getPaymentNotices().get(0);
    Debtor debtor = paymentNotice.getDebtor();

    CtEntityUniqueIdentifier entityUniqueIdentifier = new CtEntityUniqueIdentifier();
    StEntityUniqueIdentifierType identifierType =
        StEntityUniqueIdentifierType.fromValue(debtor.getUniqueIdentifier().getType().name());
    entityUniqueIdentifier.setEntityUniqueIdentifierType(identifierType);
    entityUniqueIdentifier.setEntityUniqueIdentifierValue(debtor.getUniqueIdentifier().getValue());

    DebtorInfo debtorInfo = new DebtorInfo();
    debtorInfo.setEmail(debtor.getEmail());
    debtorInfo.setFullName(debtor.getFullName());
    debtorInfo.setUniqueIdentifier(entityUniqueIdentifier);

    TipoMarcaDaBollo marcaDaBollo = new TipoMarcaDaBollo();
    marcaDaBollo.setAmount(formatEuroCentAmount(paymentNotice.getAmount()));
    marcaDaBollo.setDebtor(debtorInfo);
    marcaDaBollo.setFiscalCode(organizationFiscalCode);
    marcaDaBollo.setProvince(paymentNotice.getProvince());
    marcaDaBollo.setDocumentHash(paymentNotice.getDocumentHash().getBytes(StandardCharsets.UTF_8));

    String serviceDataXml = marshalMarcaDaBollo(marcaDaBollo);

    DemandPaymentNoticeRequest demandRequest = new DemandPaymentNoticeRequest();
    demandRequest.setIdPSP(idPsp);
    demandRequest.setIdBrokerPSP(idBrokerPsp);
    demandRequest.setIdChannel(idChannel);
    demandRequest.setIdSoggettoServizio(mbdServiceId);
    demandRequest.setPassword("PLACEHOLDER");
    demandRequest.setDatiSpecificiServizio(serviceDataXml.getBytes(StandardCharsets.UTF_8));
    return demandRequest;
  }

  public GetCartRequest mapCartRequest(
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

  public GetCartRequestV2 mapCartV2Request(
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

  public GetMbdRequestV2 mapGetMbdRequestToGetMbdRequestV2(GetMbdRequest request) {
    return GetMbdRequestV2.builder()
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

  private Debtor buildDebtor(PaymentNotice paymentNotice) {
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

  private UniqueIdentifier.UniqueIdentifierType extractUniqueIdentifierType(String fiscalCode) {
    if (Constants.FISCAL_CODE_PATTERN.matcher(fiscalCode).matches()) {
      return UniqueIdentifier.UniqueIdentifierType.F;
    } else if (Constants.VAT_NUMBER_PATTERN.matcher(fiscalCode).matches()) {
      return UniqueIdentifier.UniqueIdentifierType.G;
    } else {
      throw new IllegalArgumentException("Invalid fiscal code or VAT number format");
    }
  }

  private BigDecimal formatEuroCentAmount(long grandTotal) {
    BigDecimal amount = new BigDecimal(grandTotal);
    BigDecimal divider = new BigDecimal(100);
    return amount.divide(divider, 2, RoundingMode.UNNECESSARY);
  }

  private String marshalMarcaDaBollo(TipoMarcaDaBollo marcaDaBollo) {
    JAXBElement<TipoMarcaDaBollo> element = objectFactory.createMarcaDaBollo(marcaDaBollo);
    StringWriter writer = new StringWriter();
    jaxb2Marshaller.marshal(element, new StreamResult(writer));
    return writer.toString();
  }
}
