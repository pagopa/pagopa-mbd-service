package it.gov.pagopa.mbd.service.service.impl;

import it.gov.pagopa.mbd.service.client.ReactiveClient;
import it.gov.pagopa.mbd.service.exception.AppError;
import it.gov.pagopa.mbd.service.exception.AppException;
import it.gov.pagopa.mbd.service.exception.CartMappingException;
import it.gov.pagopa.mbd.service.exception.WebClientException;
import it.gov.pagopa.mbd.service.mapper.RequestMapper;
import it.gov.pagopa.mbd.service.model.mdb.GetMbdRequest;
import it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp.DemandPaymentNoticeResponse;
import it.gov.pagopa.mbd.service.service.MbdService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.oxm.XmlMappingException;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Set;

@Service
@Slf4j
public class MbdServiceImpl implements MbdService {

    private final Validator validator;
    private final ReactiveClient reactiveSoapClient;
    private final Jaxb2Marshaller jaxb2Marshaller;
    private final String mdbLinkBaseUrl;

    private final String idPsp;

    private final String idBrokerPsp;

    private final String channelId;

    @Autowired
    public MbdServiceImpl(
            Validator validator, ReactiveClient reactiveSoapClient,
            Jaxb2Marshaller jaxb2Marshaller,
            @Value("mbd.link.baseUrl")
            String mdbLinkBaseUrl,
            @Value("mbd.mapper.idPsp")
            String idPsp,
            @Value("mbd.mapper.idBrokerPsp")
            String idBrokerPsp,
            @Value("mbd.mapper.channelId")
            String channelId) {
        this.validator = validator;
        this.reactiveSoapClient = reactiveSoapClient;
        this.jaxb2Marshaller = jaxb2Marshaller;
        this.mdbLinkBaseUrl = mdbLinkBaseUrl;
        this.idPsp = idPsp;
        this.idBrokerPsp = idBrokerPsp;
        this.channelId = channelId;
    }

    @Override
    public Mono<ResponseEntity> getMbd(GetMbdRequest request) {
        HashMap<String, DemandPaymentNoticeResponse> hashMap = new HashMap();
        return Mono.just(request)
                .doFirst(() -> {
                    Set<ConstraintViolation<GetMbdRequest>> errors = validator.validate(request);
                    if (!errors.isEmpty()) {
                       throw new ConstraintViolationException(errors);
                    }
                })
                .onErrorMap(ConstraintViolationException.class, e -> {
                    log.error("Encountered an error during demandPaymentNotice Validation: {}", e.getMessage());
                    return e;
                })
                .map(item -> RequestMapper.mapDemandPaymentNoticeRequest(idPsp, idBrokerPsp, channelId,
                        jaxb2Marshaller, item))
                .onErrorMap(XmlMappingException.class, e -> {
                    log.error("Encountered an error during demandPaymentNotice Request Mapping: {}", e.getMessage());
                    return new AppException(AppError.PAYMENT_NOTICE_REQUEST_MAP_ERROR, e);
                })
                .flatMap(reactiveSoapClient::demandPaymentNotice)
                .onErrorMap(WebClientException.class, e -> {
                    log.error("Encountered an error during demandPaymentNotice Call: {}", e.getMessage());
                    return new AppException(AppError.PAYMENT_NOTICE_REQUEST_CALL_ERROR, e);
                })
                .map(demandPaymentNoticeResponse -> {
                    hashMap.put("demandPaymentNoticeResponse", demandPaymentNoticeResponse);
                    return RequestMapper.mapCartRequest(request, demandPaymentNoticeResponse);
                })
                .onErrorMap(CartMappingException.class, e -> {
                    log.error("Encountered an error during cart mapping: {}", e.getMessage());
                    return new AppException(AppError.CART_REQUEST_MAP_ERROR, e);
                })
                .flatMap(reactiveSoapClient::getCart)
                .onErrorMap(WebClientException.class, e -> {
                    log.error("Encountered an error during getCart Call: {}", e.getMessage());
                    return new AppException(AppError.CART_REQUEST_CALL_ERROR, e);
                })
                .map(item -> ResponseEntity.ok()
                        .header("MBD-Link",
                                StringUtils.joinWith("/", mdbLinkBaseUrl,
                                        "mbd/mbd-payment/v1", request.getPaymentNotices().get(0).getFiscalCodeEC()),
                                        "receipt", hashMap.get("demandPaymentNoticeResponse").getQrCode().getNoticeNumber())
                        .header( "MBD-NAV", hashMap.get("demandPaymentNoticeResponse").getQrCode()
                                .getNoticeNumber())
                .body(item));

    }

    @Override
    public Mono<ResponseEntity> getPaymentReceipts(String fiscalCode, String nav) {
        return Mono.zip(Mono.just(fiscalCode),Mono.just(nav).map(item -> nav.substring(1)))
                .flatMap(tuple -> reactiveSoapClient.getPaymentReceipt(tuple.getT1(),tuple.getT2()))
                .onErrorResume(e -> {
                    log.error("Encountered an error during getPaymentReceiptCall Call: {}", e.getMessage());
                    return Mono.error(new AppException(AppError.PAYMENT_RECEIPTS_CALL_ERROR, e));
                })
                .map(item -> ResponseEntity.ok()
                        .header("Content-Type", MediaType.APPLICATION_XML_VALUE)
                        .body(item.getMBDAttachment()));
    }


}
