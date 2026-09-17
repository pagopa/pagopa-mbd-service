package it.gov.pagopa.mbd.service.client;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.hibernate.validator.internal.util.Contracts.assertTrue;

import it.gov.pagopa.mbd.service.exception.DemandPaymentNoticeKOException;
import it.gov.pagopa.mbd.service.exception.WebClientException;
import it.gov.pagopa.mbd.service.model.carts.GetCartRequest;
import it.gov.pagopa.mbd.service.model.carts.GetCartRequestV2;
import it.gov.pagopa.mbd.service.model.carts.GetCartResponse;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.DemandPaymentNoticeRequest;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.DemandPaymentNoticeResponse;
import it.gov.pagopa.pagopa_api.pa.pafornode.CtReceiptV2;
import it.gov.pagopa.pagopa_api.pa.pafornode.CtTransferListPAReceiptV2;
import it.gov.pagopa.pagopa_api.pa.pafornode.CtTransferPAReceiptV2;
import it.gov.pagopa.pagopa_api.pa.pafornode.PaSendRTV2Request;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.StOutcome;
import jakarta.xml.bind.JAXBElement;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.xmlsoap.schemas.soap.envelope.Envelope;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ReactiveClient {

  private static final String OCP_SUBSCRIPTION_KEY = "ocp-apim-subscription-key";

  private final WebClient webClient;
  private final ClientDataConfig clientDataConfig;
  private final SoapEnvelopeSerializer soapEnvelopeSerializer;

  @Autowired
  public ReactiveClient(
      WebClient webClient,
      ClientDataConfig clientDataConfig,
      SoapEnvelopeSerializer soapEnvelopeSerializer) {
    this.webClient = webClient;
    this.clientDataConfig = clientDataConfig;
    this.soapEnvelopeSerializer = soapEnvelopeSerializer;
  }

  /**
   * Invokes demandPaymentNotice SOAP Nodo API
   *
   * @param request request data for demandPaymentNotice
   * @return demandPaymentNotice response wrapped in Mono
   * @throws WebClientException if the request fails or returns KO outcome
   */
  public Mono<DemandPaymentNoticeResponse> demandPaymentNotice(DemandPaymentNoticeRequest request) {

    return Mono.fromCallable(
            () -> soapEnvelopeSerializer.marshalDemandPaymentNoticeEnvelope(request))
        .doOnNext(
            requestBody -> log.debug("Requesting demandPaymentNotice with body: {}", requestBody))
        .flatMap(
            requestBody ->
                webClient
                    .post()
                    .uri(clientDataConfig.getDemandPaymentEndpoint())
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
                    .header("soapaction", "demandPaymentNotice")
                    .header(
                        OCP_SUBSCRIPTION_KEY, clientDataConfig.getDemandPaymentSubscriptionKey())
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class))
        .map(
            xml -> {
              log.debug("Received demandPaymentNotice response: {}", xml);
              return soapEnvelopeSerializer.unmarshalEnvelope(xml);
            })
        .map(this::extractDemandPaymentNoticeResponse)
        .onErrorMap(e -> new WebClientException(e.getMessage(), e));
  }

  /**
   * Invokes Checkout POST /cart v1
   *
   * @param getCartRequest request body
   * @return GetCartResponse wrapped in Mono
   * @throws WebClientException if the request fails
   */
  public Mono<GetCartResponse> getCart(GetCartRequest getCartRequest) {

    return webClient
        .post()
        .uri(clientDataConfig.getGetCartEndpoint())
        .header(OCP_SUBSCRIPTION_KEY, clientDataConfig.getGetCartSubscriptionKey())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .body(Mono.just(getCartRequest), GetCartRequest.class)
        .retrieve()
        .bodyToMono(GetCartResponse.class)
        .onErrorMap(e -> new WebClientException(e.getMessage(), e));
  }

  /**
   * Invokes Checkout POST /cart v2
   *
   * @param getCartRequest request body
   * @return GetCartResponse wrapped in Mono
   * @throws WebClientException if the request fails
   */
  public Mono<GetCartResponse> getCartV2(GetCartRequestV2 getCartRequest) {

    return webClient
        .post()
        .uri(clientDataConfig.getGetCartV2Endpoint())
        .header(OCP_SUBSCRIPTION_KEY, clientDataConfig.getGetCartSubscriptionKey())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .body(Mono.just(getCartRequest), GetCartRequest.class)
        .retrieve()
        .bodyToMono(GetCartResponse.class)
        .onErrorMap(e -> new WebClientException(e.getMessage(), e));
  }

  /**
   * Invokes GPS GET /payment-receipt/{fiscalCode}/{iuv} endpoint to retrieve the payment receipt
   *
   * @param fiscalCode organization fiscal code
   * @param iuv identificativo univoco versamento
   * @return Marca da Bollo attachment as byte array wrapped in Mono
   * @throws IllegalArgumentException if the response mapping fails
   * @throws WebClientResponseException if the request fails
   */
  public Mono<byte[]> getPaymentReceipt(String fiscalCode, String iuv) {

    return webClient
        .get()
        .uri(String.format(clientDataConfig.getGetPaymentReceiptEndpoint(), fiscalCode, iuv))
        .header(OCP_SUBSCRIPTION_KEY, clientDataConfig.getGetPaymentReceiptSubscriptionKey())
        .retrieve()
        .bodyToMono(PaSendRTV2Request.class)
        .map(this::extractMBDAttachmentIfPresent);
  }

  private byte[] extractMBDAttachmentIfPresent(PaSendRTV2Request item) {
    assertNotNull(item, "Response is null");
    CtReceiptV2 ctReceiptV2 = item.getReceipt();
    assertNotNull(ctReceiptV2, "Receipt is null");
    CtTransferListPAReceiptV2 ctTransferListPAReceiptV2 = ctReceiptV2.getTransferList();
    assertNotNull(ctTransferListPAReceiptV2, "Receipt transfer list is null");
    List<CtTransferPAReceiptV2> ctTransferPAReceiptV2 = ctTransferListPAReceiptV2.getTransfer();
    assertTrue(!ctTransferPAReceiptV2.isEmpty(), "Missing receipt transfer");
    assertNotNull(
        ctTransferPAReceiptV2.get(0).getMBDAttachment(), "Receipt transfer MBD attachment is null");
    return ctTransferPAReceiptV2.get(0).getMBDAttachment();
  }

  private DemandPaymentNoticeResponse extractDemandPaymentNoticeResponse(Envelope envelope) {
    DemandPaymentNoticeResponse response = null;
    if (envelope.getBody() != null
        && envelope.getBody().getAny() != null
        && !envelope.getBody().getAny().isEmpty()) {

      Object first = envelope.getBody().getAny().get(0);
      if (first instanceof JAXBElement<?> jaxbElement
          && jaxbElement.getValue() instanceof DemandPaymentNoticeResponse demand) {
        response = demand;
      } else if (first instanceof DemandPaymentNoticeResponse demand) {
        response = demand;
      }
    }

    if (response == null || StOutcome.KO.equals(response.getOutcome())) {
      throw new DemandPaymentNoticeKOException(
          "Encountered KO while calling demandPayment", response);
    }

    return response;
  }
}
