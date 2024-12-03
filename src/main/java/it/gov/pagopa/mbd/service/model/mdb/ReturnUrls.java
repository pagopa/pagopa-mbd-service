package it.gov.pagopa.mbd.service.model.mdb;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReturnUrls {

    @NotBlank
    private String successUrl;
    @NotBlank
    private String cancelUrl;
    @NotBlank
    private String errorUrl;

}
