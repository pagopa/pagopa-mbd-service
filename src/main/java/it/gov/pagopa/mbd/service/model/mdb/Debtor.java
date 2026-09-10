package it.gov.pagopa.mbd.service.model.mdb;

import io.swagger.v3.oas.annotations.media.Schema;
import it.gov.pagopa.mbd.service.config.ValidUniqueIdentifier;
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
public class Debtor {

  @Schema(example = "Mario Rossi", description = "Debtor full name.")
  @NotBlank
  private String fullName;

  @ValidUniqueIdentifier @Valid private UniqueIdentifier uniqueIdentifier;

  @Schema(example = "mario.rossi@test.test", description = "Debtor mail.")
  @NotBlank
  private String email;
}
