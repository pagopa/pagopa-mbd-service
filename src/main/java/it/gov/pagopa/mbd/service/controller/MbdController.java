package it.gov.pagopa.mbd.service.controller;

import it.gov.pagopa.mbd.service.exception.AppException;
import it.gov.pagopa.mbd.service.model.carts.GetCartErrorResponse;
import it.gov.pagopa.mbd.service.service.MdbService;
import it.gov.pagopa.mbd.service.model.mdb.GetMdbRequest;
import jakarta.websocket.server.PathParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@RestController
public class MbdController {

    private final MdbService mdbService;

    public MbdController(MdbService mdbService) {
        this.mdbService = mdbService;
    }

    @PostMapping(value = "/mbd", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity> getMdb(@RequestBody GetMdbRequest request) {
        return mdbService.getMdb(request).onErrorResume(e -> {
            if (e instanceof AppException) {
                return Mono.just(ResponseEntity.status(((AppException) e).getHttpStatus())
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .body(GetCartErrorResponse.builder().code(((AppException) e).getTitle())
                                .message(e.getMessage())
                                .errorUrl(request.getReturnUrls().getErrorUrl()).build()));
            }
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).header("Content-Type",
                    MediaType.APPLICATION_JSON_VALUE).body(GetCartErrorResponse.builder()
                    .errorUrl(request.getReturnUrls().getErrorUrl()).build()));
        });
    }

    @GetMapping(value = "/mbd-payments/{fiscalCode}/receipt/{nav}", produces = MediaType.APPLICATION_XML_VALUE)
    public Mono<ResponseEntity> getPaymentReceipts(@PathParam("fiscalCode") String fiscalCode,
                                                   @PathParam("nav") String nav) {
        return mdbService.getPaymentReceipts(fiscalCode, nav);
    }

}
