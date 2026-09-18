package it.gov.pagopa.mbd.service.exception;

import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.validation.annotation.Validated;

@EqualsAndHashCode(callSuper = true)
@Value
@Validated
public class CartRequestMappingException extends RuntimeException {

  public CartRequestMappingException(@NotNull String message, Throwable cause) {
    super(message, cause);
  }
}
