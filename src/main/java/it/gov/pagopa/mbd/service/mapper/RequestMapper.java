package it.gov.pagopa.mbd.service.mapper;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.hibernate.validator.internal.util.Contracts.assertTrue;

import it.gov.pagopa.mbd.service.client.SoapEnvelopeSerializer;
import it.gov.pagopa.mbd.service.exception.CartRequestMappingException;
import it.gov.pagopa.mbd.service.model.carts.CartPaymentNotice;
import it.gov.pagopa.mbd.service.model.carts.CartReturnUrls;
import it.gov.pagopa.mbd.service.model.carts.CartReturnUrlsV2;
import it.gov.pagopa.mbd.service.model.carts.GetCartRequest;
import it.gov.pagopa.mbd.service.model.carts.GetCartRequestV2;
import it.gov.pagopa.mbd.service.model.mdb.Debtor;
import it.gov.pagopa.mbd.service.model.mdb.GetMbdRequest;
import it.gov.pagopa.mbd.service.model.mdb.CreateMbdRequestV2;
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
import it.gov.pagopa.pagopa_api.pa.marcadabollo.TipoMarcaDaBollo;
import it.gov.pagopa.pagopa_api.pa.pafornode.CtEntityUniqueIdentifier;
import it.gov.pagopa.pagopa_api.pa.pafornode.StEntityUniqueIdentifierType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Maps application-level DTOs (v1/v2 Marca da Bollo requests and Nodo/Checkout responses) into the
 * JAXB-generated payloads used to interact with the Nodo {@code demandPaymentNotice} SOAP API and
 * with the Checkout {@code /cart} endpoint. PSP identifiers and the {@code idSoggettoServizio} are
 * injected from configuration, while the {@code datiSpecificiServizio} SOAP field is populated by
 * delegating the {@link TipoMarcaDaBollo} marshalling to {@link SoapEnvelopeSerializer}.
 */
@Component
public class RequestMapper {

  private final SoapEnvelopeSerializer soapEnvelopeSerializer;
  private final String idPsp;
  private final String idBrokerPsp;
  private final String channelId;
  private final String mbdServiceId;

  public RequestMapper(
      SoapEnvelopeSerializer soapEnvelopeSerializer,
      @Value("${mbd.mapper.idPsp}") String idPsp,
      @Value("${mbd.mapper.idBrokerPsp}") String idBrokerPsp,
      @Value("${mbd.mapper.channelId}") String channelId,
      @Value("${mbd.service.id}") String mbdServiceId) {
    this.soapEnvelopeSerializer = soapEnvelopeSerializer;
    this.idPsp = idPsp;
    this.idBrokerPsp = idBrokerPsp;
    this.channelId = channelId;
    this.mbdServiceId = mbdServiceId;
  }

  /**
   * Builds the {@link DemandPaymentNoticeRequest} payload for the Nodo {@code demandPaymentNotice}
   * SOAP operation, starting from the incoming application-level {@link CreateMbdRequestV2}. Populates
   * PSP identifiers from configuration and marshals the {@link TipoMarcaDaBollo} service data into
   * the {@code datiSpecificiServizio} field.
   *
   * @param organizationFiscalCode fiscal code of the creditor organization (PA)
   * @param mdbRequest incoming Marca da Bollo request in its V2 shape
   * @return the JAXB-typed {@link DemandPaymentNoticeRequest} ready to be sent to the Nodo
   * @throws org.springframework.oxm.XmlMappingException if the service data cannot be marshalled
   */
  public DemandPaymentNoticeRequest mapDemandPaymentNoticeRequest(
      String organizationFiscalCode, CreateMbdRequestV2 mdbRequest) {

    PaymentNoticeV2 paymentNotice = mdbRequest.getPaymentNotices().get(0);
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

    String serviceDataXml = soapEnvelopeSerializer.marshalMarcaDaBollo(marcaDaBollo);

    DemandPaymentNoticeRequest demandRequest = new DemandPaymentNoticeRequest();
    demandRequest.setIdPSP(idPsp);
    demandRequest.setIdBrokerPSP(idBrokerPsp);
    demandRequest.setIdChannel(channelId);
    demandRequest.setIdSoggettoServizio(mbdServiceId);
    demandRequest.setPassword("PLACEHOLDER");
    demandRequest.setDatiSpecificiServizio(serviceDataXml.getBytes(StandardCharsets.UTF_8));
    return demandRequest;
  }

  /**
   * Builds the Checkout v1 {@link GetCartRequest} starting from the original {@link GetMbdRequest}
   * and the response received from the Nodo {@code demandPaymentNotice} call. Ensures that all
   * mandatory fields (payment options, QR code) are present in the response.
   *
   * @param request original Marca da Bollo request (v1)
   * @param demandPaymentNoticeResponse response returned by the Nodo
   * @return the {@link GetCartRequest} to be sent to Checkout
   * @throws CartRequestMappingException if the response is missing required fields or the mapping
   *     fails
   */
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
      throw new CartRequestMappingException(e.getMessage(), e);
    }
  }

  /**
   * Builds the Checkout v2 {@link GetCartRequestV2} starting from the {@link CreateMbdRequestV2} and
   * the response received from the Nodo {@code demandPaymentNotice} call. Ensures that all
   * mandatory fields (payment options, QR code) are present in the response.
   *
   * @param request original Marca da Bollo request (v2)
   * @param demandPaymentNoticeResponse response returned by the Nodo
   * @return the {@link GetCartRequestV2} to be sent to Checkout
   * @throws CartRequestMappingException if the response is missing required fields or the mapping
   *     fails
   */
  public GetCartRequestV2 mapCartV2Request(
          CreateMbdRequestV2 request, DemandPaymentNoticeResponse demandPaymentNoticeResponse) {
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
      throw new CartRequestMappingException(e.getMessage(), e);
    }
  }

  /**
   * Adapts a v1 {@link GetMbdRequest} to the v2 shape ({@link CreateMbdRequestV2}), so the internal
   * pipeline can operate on a single model. Extracts debtor identity from first/last name and
   * fiscal code and detects whether it is a natural person or a legal entity.
   *
   * @param request v1 Marca da Bollo request
   * @return the equivalent v2 representation
   */
  public CreateMbdRequestV2 mapGetMbdRequestToGetMbdRequestV2(GetMbdRequest request) {
    return CreateMbdRequestV2.builder()
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

  /**
   * Builds a {@link Debtor} from a v1 {@link PaymentNotice}, composing the full name and inferring
   * the {@link UniqueIdentifier} type (natural person vs. legal entity) from the fiscal code.
   */
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

  /**
   * Detects whether the given identifier is an Italian natural-person fiscal code ({@code F}) or a
   * legal-entity VAT number ({@code G}), based on the patterns declared in {@link Constants}.
   *
   * @throws IllegalArgumentException if the value matches neither pattern
   */
  private UniqueIdentifier.UniqueIdentifierType extractUniqueIdentifierType(String fiscalCode) {
    if (Constants.FISCAL_CODE_PATTERN.matcher(fiscalCode).matches()) {
      return UniqueIdentifier.UniqueIdentifierType.F;
    } else if (Constants.VAT_NUMBER_PATTERN.matcher(fiscalCode).matches()) {
      return UniqueIdentifier.UniqueIdentifierType.G;
    } else {
      throw new IllegalArgumentException("Invalid fiscal code or VAT number format");
    }
  }

  /**
   * Converts an amount expressed in euro cents (long) into a {@link BigDecimal} with 2 decimal
   * digits (euro). Uses {@link RoundingMode#UNNECESSARY}: the conversion is exact by construction
   * (division by 100), so any rounding would signal a programming error.
   */
  private BigDecimal formatEuroCentAmount(long grandTotal) {
    BigDecimal amount = new BigDecimal(grandTotal);
    BigDecimal divider = new BigDecimal(100);
    return amount.divide(divider, 2, RoundingMode.UNNECESSARY);
  }
}
