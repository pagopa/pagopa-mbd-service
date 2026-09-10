package it.gov.pagopa.mbd.service.client;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.hibernate.validator.internal.util.Contracts.assertTrue;

import it.gov.pagopa.mbd.service.exception.WebClientException;
import it.gov.pagopa.mbd.service.model.carts.GetCartRequest;
import it.gov.pagopa.mbd.service.model.carts.GetCartRequestV2;
import it.gov.pagopa.mbd.service.model.carts.GetCartResponse;
import it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp.DemandPaymentNoticeRequest;
import it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp.DemandPaymentNoticeResponse;
import it.gov.pagopa.mbd.service.model.xml.node.pafornode.CtReceiptV2;
import it.gov.pagopa.mbd.service.model.xml.node.pafornode.CtTransferListPAReceiptV2;
import it.gov.pagopa.mbd.service.model.xml.node.pafornode.CtTransferPAReceiptV2;
import it.gov.pagopa.mbd.service.model.xml.node.pafornode.PaSendRTV2Request;
import it.gov.pagopa.mbd.service.model.xml.node.soap.envelope.Envelope;
import it.gov.pagopa.mbd.service.model.xml.xsd.common_types.v1_0.StOutcome;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Component
public class ReactiveClient {

  private static final String OCP_SUBSCRIPTION_KEY = "ocp-apim-subscription-key";
  private static final String DEMAND_PAYMENT_BODY =
      """
    <?xml version="1.0" encoding="utf-8"?>
      <Envelope xmlns="http://schemas.xmlsoap.org/soap/envelope/">
        <Body>
          <demandPaymentNoticeRequest
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xmlns="http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd">
            <idPSP xmlns="">%s</idPSP>
            <idBrokerPSP xmlns="">%s</idBrokerPSP>
            <idChannel xmlns="">%s</idChannel>
            <idSoggettoServizio xmlns="">%s</idSoggettoServizio>
            <datiSpecificiServizio xmlns="">%s</datiSpecificiServizio>
          </demandPaymentNoticeRequest>
        </Body>
      </Envelope>""";

  private final WebClient webClient;
  private final ClientDataConfig clientDataConfig;

  @Autowired
  public ReactiveClient(WebClient webClient, ClientDataConfig clientDataConfig) {
    this.webClient = webClient;
    this.clientDataConfig = clientDataConfig;
  }

  /**
   * Invokes demandPaymentNotice SOAP Nodo API
   *
   * @param request request data for demandPaymentNotice
   * @return demandPaymentNotice response wrapped in Mono
   * @throws WebClientException if the request fails or returns KO outcome
   */
  public Mono<DemandPaymentNoticeResponse> demandPaymentNotice(DemandPaymentNoticeRequest request) {

    String requestBody =
        String.format(
            DEMAND_PAYMENT_BODY,
            request.getIdPSP(),
            request.getIdBrokerPSP(),
            request.getIdChannel(),
            request.getIdSoggettoServizio(),
            new String(request.getDatiSpecificiServizio()));

    return webClient
        .post()
        .uri(clientDataConfig.getDemandPaymentEndpoint())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
        .header("soapaction", "demandPaymentNotice")
        .header(OCP_SUBSCRIPTION_KEY, clientDataConfig.getDemandPaymentSubscriptionKey())
        .bodyValue(requestBody)
        .retrieve()
        .bodyToMono(Envelope.class)
        .map(
            item -> {
              if (item.getBody() == null
                  || item.getBody().getDemandPaymentNoticeResponse() == null
                  || StOutcome.KO.equals(
                      item.getBody().getDemandPaymentNoticeResponse().getOutcome())) {
                throw new RuntimeException(
                    "Encountered KO while calling demandPayment "
                        + (item.getBody() != null
                                && item.getBody().getDemandPaymentNoticeResponse() != null
                                && item.getBody().getDemandPaymentNoticeResponse().getFault()
                                    != null
                            ? item.getBody()
                                    .getDemandPaymentNoticeResponse()
                                    .getFault()
                                    .getFaultCode()
                                + " - "
                                + item.getBody()
                                    .getDemandPaymentNoticeResponse()
                                    .getFault()
                                    .getDescription()
                            : ""));
              }
              return item.getBody().getDemandPaymentNoticeResponse();
            })
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
   * @throws WebClientException if the request fails
   */
  public Mono<byte[]> getPaymentReceipt(String fiscalCode, String iuv) {

    return webClient
        .get()
        .uri(String.format(clientDataConfig.getGetPaymentReceiptEndpoint(), fiscalCode, iuv))
        .header(OCP_SUBSCRIPTION_KEY, clientDataConfig.getGetPaymentReceiptSubscriptionKey())
        .retrieve()
        .bodyToMono(PaSendRTV2Request.class)
        .map(
            item -> {
              assertNotNull(item);
              CtReceiptV2 ctReceiptV2 = item.getReceipt();
              assertNotNull(ctReceiptV2);
              CtTransferListPAReceiptV2 ctTransferListPAReceiptV2 = ctReceiptV2.getTransferList();
              assertNotNull(ctTransferListPAReceiptV2);
              List<CtTransferPAReceiptV2> ctTransferPAReceiptV2 =
                  ctTransferListPAReceiptV2.getTransfer();
              assertTrue(!ctTransferPAReceiptV2.isEmpty(), "Missing ctTransferPAReceiptV2");
              assertNotNull(ctTransferPAReceiptV2.get(0).getMBDAttachment());
              return ctTransferPAReceiptV2.get(0).getMBDAttachment();
            })
        .onErrorMap(IllegalArgumentException.class, e -> e)
        .onErrorMap(
            WebClientResponseException.class, e -> new WebClientException(e.getMessage(), e));
  }
}
