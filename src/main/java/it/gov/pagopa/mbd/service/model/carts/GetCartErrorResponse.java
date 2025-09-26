package it.gov.pagopa.mbd.service.model.carts;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetCartErrorResponse {

  @Schema(example = "https://url.it", description = "The error URL provided in the request.")
  private String errorUrl;
}
