package it.gov.pagopa.mbd.service.model.mdb;

import io.swagger.v3.oas.annotations.media.Schema;
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

  @Schema(example = "Mario", description = "Debtor name.")
  @NotBlank
  private String firstName;

  @Schema(example = "Rossi", description = "Debtor surname.")
  @NotBlank
  private String lastName;

  @Schema(example = "00000000000000000", description = "Debtor fiscal code.")
  @NotBlank
  private String fiscalCode;

  @Schema(example = "mario.rossi@test.test", description = "Debtor mail.")
  @NotBlank
  private String email;

  @Schema(example = "16", description = "MBD amount.")
  @NotNull
  private Long amount;

  @Schema(example = "RM", description = "Debtor province.")
  @NotBlank
  private String province;

  @Schema(description = "Document hash.")
  @Size(min = 44, max = 44)
  private String documentHash;
}
