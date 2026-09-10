package it.gov.pagopa.mbd.service.service;

import it.gov.pagopa.mbd.service.model.mdb.GetMbdRequest;
import it.gov.pagopa.mbd.service.model.mdb.GetMbdRequestV2;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

/** Service for Marca da Bollo business logic */
public interface MbdService {

  /**
   * Creates the debt position and pays the Marca da Bollo for the provided document hash
   *
   * @param fiscalCodeEC organization fiscal code
   * @param request data to create the debt position and pay the Marca da Bollo
   * @return a Mono of ResponseEntity containing the redirect url for payment and the redirect url
   *     for retrieving Marca da Bollo
   */
  Mono<ResponseEntity> getMbd(String fiscalCodeEC, GetMbdRequest request);

  /**
   * Creates the debt position and pays the Marca da Bollo for the provided document hash
   *
   * @param fiscalCodeEC organization fiscal code
   * @param request data to create the debt position and pay the Marca da Bollo
   * @return a Mono of ResponseEntity containing the redirect url for payment and the redirect url
   *     for retrieving Marca da Bollo
   */
  Mono<ResponseEntity> getMbdV2(String fiscalCodeEC, GetMbdRequestV2 request);

  /**
   * Retrieves the Marca da Bollo receipts for the provided fiscal code and nav
   *
   * @param fiscalCode organization fiscal code
   * @param nav notice number of the pay debt position
   * @return a Mono of ResponseEntity containing the Marca da Bollo receipt
   */
  Mono<ResponseEntity> getPaymentReceipts(String fiscalCode, String nav);
}
