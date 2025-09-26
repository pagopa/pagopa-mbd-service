package it.gov.pagopa.mbd.service.model.mdb;

import io.swagger.v3.oas.annotations.media.Schema;
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

  @Schema(example = "https://url1.it", description = "Redirect success URL.")
  @NotBlank
  private String successUrl;

  @Schema(example = "https://url2.it", description = "Redirect cancel URL.")
  @NotBlank
  private String cancelUrl;

  @Schema(example = "https://url3.it", description = "Redirect error URL.")
  @NotBlank
  private String errorUrl;
}
