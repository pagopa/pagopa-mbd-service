package it.gov.pagopa.mbd.service.model.mdb;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentNotice {

    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @NotBlank
    private String fiscalCode;
    @NotBlank
    private String email;
    @NotBlank
    private String fiscalCodeEC;
    @NotNull
    private Long amount;
    @NotBlank
    private String province;

    @Size(min = 44, max = 44)
    private String documentHash;

}
