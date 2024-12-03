package it.gov.pagopa.mbd.service.exception;

import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.validation.annotation.Validated;

@EqualsAndHashCode(callSuper = true)
@Value
@Validated
public class WebClientException extends RuntimeException {

    public WebClientException(@NotNull String message,
                              Throwable cause) {
        super(message, cause);
    }

}
