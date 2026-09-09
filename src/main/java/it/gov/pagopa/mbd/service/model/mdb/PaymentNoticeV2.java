package it.gov.pagopa.mbd.service.model.mdb;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentNoticeV2 {

  @NotNull
  @Valid
  private Debtor debtor;

  @Schema(example = "16", description = "MBD amount.")
  @NotNull
  private Long amount;

  @Schema(example = "RM", description = "Organization province.")
  @NotBlank
  private String province;

  @Schema(description = "Document hash.")
  @Size(min = 44, max = 44)
  @NotNull
  private String documentHash;
}
