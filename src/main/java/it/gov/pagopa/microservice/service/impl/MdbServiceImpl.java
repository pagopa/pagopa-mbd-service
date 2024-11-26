package it.gov.pagopa.microservice.service.impl;

import it.gov.pagopa.microservice.client.ReactiveClient;
import it.gov.pagopa.microservice.exception.AppError;
import it.gov.pagopa.microservice.exception.AppException;
import it.gov.pagopa.microservice.mapper.RequestMapper;
import it.gov.pagopa.microservice.model.carts.GetCartResponse;
import it.gov.pagopa.microservice.model.mdb.GetMdbRequest;
import it.gov.pagopa.microservice.model.xml.node.nodeforpsp.DemandPaymentNoticeResponse;
import it.gov.pagopa.microservice.service.MdbService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.HashMap;

@Service
@Slf4j
public class MdbServiceImpl implements MdbService {

    private final ReactiveClient reactiveSoapClient;
    private final Jaxb2Marshaller jaxb2Marshaller;
    private final String mdbLinkBaseUrl;

    @Autowired
    public MdbServiceImpl(
            ReactiveClient reactiveSoapClient,
            Jaxb2Marshaller jaxb2Marshaller,
            @Value("")
            String mdbLinkBaseUrl) {
        this.reactiveSoapClient = reactiveSoapClient;
        this.jaxb2Marshaller = jaxb2Marshaller;
        this.mdbLinkBaseUrl = mdbLinkBaseUrl;
    }

    @Override
    public Mono<ServerResponse> getMdb(GetMdbRequest request) {
        HashMap<String, DemandPaymentNoticeResponse> hashMap = new HashMap();
        return Mono.just(request)
                .map(item -> RequestMapper.mapDemandPaymentNoticeRequest(jaxb2Marshaller, item))
                .onErrorResume(e -> {
                    log.error("Encountered an error during demandPaymentNotice Request Mapping: {}", e.getMessage());
                    return Mono.error(new AppException(AppError.PAYMENT_NOTICE_REQUEST_MAP_ERROR, e));
                })
                .flatMap(reactiveSoapClient::demandPaymentNotice)
                .onErrorResume(e -> {
                    log.error("Encountered an error during demandPaymentNotice Call: {}", e.getMessage());
                    return Mono.error(new AppException(AppError.PAYMENT_NOTICE_REQUEST_CALL_ERROR, e));
                })
                .map(demandPaymentNoticeResponse -> {
                    hashMap.put("demandPaymentResponse", demandPaymentNoticeResponse);
                    return RequestMapper.mapCartRequest(request, demandPaymentNoticeResponse);
                })
                .onErrorResume(e -> {
                    log.error("Encountered an error during cart mapping: {}", e.getMessage());
                    return Mono.error(new AppException(AppError.CART_REQUEST_MAP_ERROR, e));
                })
                .flatMap(reactiveSoapClient::getCart)
                .onErrorResume(e -> {
                    log.error("Encountered an error during getCart Call: {}", e.getMessage());
                    return Mono.error(new AppException(AppError.CART_REQUEST_CALL_ERROR, e));
                })
                .transformDeferred(item -> ServerResponse.ok()
                        .header("MBD-Link",
                                StringUtils.joinWith("/", mdbLinkBaseUrl,
                                        "mbd/mbd-payment/v1", request.getPaymentNotices().get(0).getFiscalCodeEC()),
                                        "receipt", hashMap.get("demandPaymentNoticeResponse").getQrCode().getNoticeNumber())
                        .header( "MBD-NAV", hashMap.get("demandPaymentNoticeResponse").getQrCode()
                                .getNoticeNumber())
                .body(Mono.just(item), GetCartResponse.class));
    }

    @Override
    public Mono<ServerResponse> getPaymentReceipts(String fiscalCode, String nav) {
        return Mono.zip(Mono.just(fiscalCode),Mono.just(nav).map(item -> nav.substring(1)))
                .flatMap(tuple -> reactiveSoapClient.getPaymentReceipt(tuple.getT1(),tuple.getT2()))
                .onErrorResume(e -> {
                    log.error("Encountered an error during getPaymentReceiptCall Call: {}", e.getMessage());
                    return Mono.error(new AppException(AppError.PAYMENT_RECEIPTS_CALL_ERROR, e));
                })
                .flatMap(item -> ServerResponse.ok().bodyValue(item.getMBDAttachment()));
    }

}
