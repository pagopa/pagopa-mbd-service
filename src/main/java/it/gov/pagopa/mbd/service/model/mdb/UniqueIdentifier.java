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
public class UniqueIdentifier {

  @Schema(
      example = "F",
      description = "Describe if the debtor is a Natural Person (''F') or a Legal Entity ('G').")
  @NotNull
  private UniqueIdentifierType type;

  @Schema(
      example = "00000000000000000",
      description = "Fiscal Code for Natural Persons ('F'), VAT number for Legal Entities ('G')")
  @NotBlank
  private String value;

  /**
   * Represents the legal entity type. 'F' - Natural Person (Persona Fisica) 'G' - Legal Entity
   * (Persona Giuridica)
   */
  public enum UniqueIdentifierType {
    F,
    G
  }
}
