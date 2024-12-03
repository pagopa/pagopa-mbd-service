package it.gov.pagopa.mbd.service.service;

import it.gov.pagopa.mbd.service.model.mdb.GetMbdRequest;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public interface MbdService {

    Mono<ResponseEntity> getMbd(GetMbdRequest request);

    Mono<ResponseEntity> getPaymentReceipts(String fiscalCode, String nav);

}
