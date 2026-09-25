package it.gov.pagopa.mbd.service.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.mbd.service.client.ReactiveClient;
import it.gov.pagopa.mbd.service.exception.AppError;
import it.gov.pagopa.mbd.service.exception.AppException;
import it.gov.pagopa.mbd.service.exception.CartRequestMappingException;
import it.gov.pagopa.mbd.service.exception.WebClientException;
import it.gov.pagopa.mbd.service.mapper.RequestMapper;
import it.gov.pagopa.mbd.service.model.ProblemJson;
import it.gov.pagopa.mbd.service.model.carts.GetCartResponse;
import it.gov.pagopa.mbd.service.model.mdb.GetMbdRequest;
import it.gov.pagopa.mbd.service.model.mdb.CreateMbdRequestV2;
import it.gov.pagopa.mbd.service.model.mdb.GetMdbReceipt;
import it.gov.pagopa.mbd.service.service.MbdService;
import it.gov.pagopa.mbd.service.util.ApiPaths;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.DemandPaymentNoticeResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.oxm.XmlMappingException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class MbdServiceImpl implements MbdService {

  private static final String DEMAND_PAYMENT_NOTICE_RESPONSE_KEY = "demandPaymentNoticeResponse";

  private final Validator validator;
  private final ReactiveClient reactiveSoapClient;
  private final RequestMapper requestMapper;
  private final String mdbLinkBaseUrl;
  private final ObjectMapper objectMapper;

  @Autowired
  public MbdServiceImpl(
      Validator validator,
      ReactiveClient reactiveSoapClient,
      RequestMapper requestMapper,
      ObjectMapper objectMapper,
      @Value("${mbd.link.baseUrl}") String mdbLinkBaseUrl) {
    this.validator = validator;
    this.reactiveSoapClient = reactiveSoapClient;
    this.requestMapper = requestMapper;
    this.objectMapper = objectMapper;
    this.mdbLinkBaseUrl = mdbLinkBaseUrl;
  }

  /** {@inheritDoc} */
  @Override
  public Mono<GetCartResponse> getMbd(String organizationFiscalCode, GetMbdRequest request) {
    HashMap<String, DemandPaymentNoticeResponse> hashMap = new HashMap<>();
    return Mono.just(request)
        .doFirst(
            () -> {
              Set<ConstraintViolation<GetMbdRequest>> errors = validator.validate(request);
              if (!errors.isEmpty()) {
                throw new ConstraintViolationException(errors);
              }
            })
        .onErrorMap(
            ConstraintViolationException.class,
            e -> {
              log.error(
                  "Encountered an error during demandPaymentNotice validation: {}", e.getMessage());
              return e;
            })
        .map(
            item -> {
              CreateMbdRequestV2 createMbdRequestV2 =
                  requestMapper.mapGetMbdRequestToGetMbdRequestV2(item);
              return requestMapper.mapDemandPaymentNoticeRequest(
                  organizationFiscalCode, createMbdRequestV2);
            })
        .onErrorMap(
            XmlMappingException.class,
            e -> {
              log.error(
                  "Encountered an error during demandPaymentNotice request mapping: {}",
                  e.getMessage());
              return new AppException(AppError.PAYMENT_NOTICE_REQUEST_MAP_ERROR, e);
            })
        .flatMap(reactiveSoapClient::demandPaymentNotice)
        .onErrorMap(
            WebClientException.class,
            e -> {
              log.error("Encountered an error during demandPaymentNotice call: {}", e.getMessage());
              return new AppException(AppError.PAYMENT_NOTICE_REQUEST_CALL_ERROR, e);
            })
        .map(
            demandPaymentNoticeResponse -> {
              hashMap.put(DEMAND_PAYMENT_NOTICE_RESPONSE_KEY, demandPaymentNoticeResponse);
              return requestMapper.mapCartRequest(request, demandPaymentNoticeResponse);
            })
        .onErrorMap(
            CartRequestMappingException.class,
            e -> {
              log.error("Encountered an error during cart request mapping: {}", e.getMessage());
              return new AppException(AppError.CART_REQUEST_MAP_ERROR, e);
            })
        .flatMap(reactiveSoapClient::getCart)
        .onErrorMap(
            WebClientException.class,
            e -> {
              log.error("Encountered an error during getCart call: {}", e.getMessage());
              return new AppException(AppError.CART_REQUEST_CALL_ERROR, e);
            })
        .map(
            item -> {
              String noticeNumber =
                  hashMap.get(DEMAND_PAYMENT_NOTICE_RESPONSE_KEY).getQrCode().getNoticeNumber();
              item.setNav(noticeNumber);
              String mbdDownloadLink =
                  buildMbdDownloadLink(
                      ApiPaths.V1_BASE + ApiPaths.V1_MBD_RECEIPT,
                      organizationFiscalCode,
                      noticeNumber);
              item.setMbdDownloadLink(mbdDownloadLink);
              return item;
            });
  }

  /** {@inheritDoc} */
  @Override
  public Mono<GetCartResponse> getMbdV2(String organizationFiscalCode, CreateMbdRequestV2 request) {
    HashMap<String, DemandPaymentNoticeResponse> hashMap = new HashMap<>();
    return Mono.just(request)
        .doFirst(
            () -> {
              Set<ConstraintViolation<CreateMbdRequestV2>> errors = validator.validate(request);
              if (!errors.isEmpty()) {
                throw new ConstraintViolationException(errors);
              }
            })
        .onErrorMap(
            ConstraintViolationException.class,
            e -> {
              log.error(
                  "Encountered an error during demandPaymentNotice validation: {}", e.getMessage());
              return e;
            })
        .map(item -> requestMapper.mapDemandPaymentNoticeRequest(organizationFiscalCode, item))
        .onErrorMap(
            XmlMappingException.class,
            e -> {
              log.error(
                  "Encountered an error during demandPaymentNotice request mapping: {}",
                  e.getMessage());
              return new AppException(AppError.PAYMENT_NOTICE_REQUEST_MAP_ERROR, e);
            })
        .flatMap(reactiveSoapClient::demandPaymentNotice)
        .onErrorMap(
            WebClientException.class,
            e -> {
              log.error("Encountered an error during demandPaymentNotice call: {}", e.getMessage());
              return new AppException(AppError.PAYMENT_NOTICE_REQUEST_CALL_ERROR, e);
            })
        .map(
            demandPaymentNoticeResponse -> {
              hashMap.put(DEMAND_PAYMENT_NOTICE_RESPONSE_KEY, demandPaymentNoticeResponse);
              return requestMapper.mapCartV2Request(request, demandPaymentNoticeResponse);
            })
        .onErrorMap(
            CartRequestMappingException.class,
            e -> {
              log.error("Encountered an error during cart request mapping: {}", e.getMessage());
              return new AppException(AppError.CART_REQUEST_MAP_ERROR, e);
            })
        .flatMap(reactiveSoapClient::getCartV2)
        .onErrorMap(
            WebClientException.class,
            e -> {
              log.error("Encountered an error during getCart call: {}", e.getMessage());
              return new AppException(AppError.CART_REQUEST_CALL_ERROR, e);
            })
        .map(
            item -> {
              String noticeNumber =
                  hashMap.get(DEMAND_PAYMENT_NOTICE_RESPONSE_KEY).getQrCode().getNoticeNumber();
              item.setNav(noticeNumber);
              String mbdDownloadLink =
                  buildMbdDownloadLink(
                      ApiPaths.V2_BASE + ApiPaths.V2_MBD_RECEIPT,
                      organizationFiscalCode,
                      noticeNumber);
              item.setMbdDownloadLink(mbdDownloadLink);
              return item;
            });
  }

  /** {@inheritDoc} */
  @Override
  public Mono<GetMdbReceipt> getPaymentReceipts(String organizationFiscalCode, String nav) {
    return Mono.zip(Mono.just(organizationFiscalCode), Mono.just(nav).map(item -> nav.substring(1)))
        .flatMap(tuple -> reactiveSoapClient.getPaymentReceipt(tuple.getT1(), tuple.getT2()))
        .onErrorMap(
            WebClientResponseException.class,
            e -> {
              int status = e.getStatusCode().value();
              String detail = hasProblemJsonBody(status) ? getProblemJsonDetail(e) : e.getMessage();
              log.error(
                  "Encountered an error during getPaymentReceipt call: status={}, detail={}",
                  status,
                  detail);

              if (status == 404) {
                return new AppException(
                    AppError.PAYMENT_RECEIPTS_NOT_FOUND, e, organizationFiscalCode, nav);
              }
              return new AppException(AppError.PAYMENT_RECEIPTS_CALL_ERROR, e, detail);
            })
        .onErrorMap(
            IllegalArgumentException.class,
            e -> {
              log.error("Encountered an error extracting receipt content: {}", e.getMessage());
              return new AppException(
                  AppError.PAYMENT_RECEIPTS_RESPONSE_MAPPING_ERROR, e, e.getMessage());
            })
        .map(item -> GetMdbReceipt.builder().content(item).build());
  }

  /**
   * Builds the absolute Marca da Bollo download URL by concatenating the configured base URL with
   * the given API path template (using the same {@code {name}} placeholders declared in the
   * controllers) and expanding the {@code organization-fiscal-code} / {@code nav} variables.
   */
  private String buildMbdDownloadLink(
      String pathTemplate, String organizationFiscalCode, String noticeNumber) {
    return UriComponentsBuilder.fromUriString(mdbLinkBaseUrl)
        .path(pathTemplate)
        .buildAndExpand(
            Map.of(
                "organization-fiscal-code", organizationFiscalCode,
                "nav", noticeNumber))
        .toUriString();
  }

  private boolean hasProblemJsonBody(int status) {
    return status == 404 || status == 422 || status == 500;
  }

  private String getProblemJsonDetail(WebClientResponseException e) {
    byte[] body = e.getResponseBodyAsByteArray();
    if (body.length == 0) {
      return "";
    }
    try {
      ProblemJson problem = objectMapper.readValue(body, ProblemJson.class);
      return problem != null && problem.getDetail() != null ? problem.getDetail() : "";
    } catch (IOException ex) {
      log.debug("Upstream error body is not a ProblemJson", ex);
      return "";
    }
  }
}
