package it.gov.pagopa.mbd.service.service;

import it.gov.pagopa.mbd.service.model.mdb.GetMdbRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public interface MdbService {

    Mono<ResponseEntity> getMdb(GetMdbRequest request);

    Mono<ResponseEntity> getPaymentReceipts(String fiscalCode, String nav);

}
