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
public class GetCartResponse {

  @Schema(
      example = "https://dev.checkout.pagopa.it.it/c/123",
      description = "Redirect URL to Checkout page.")
  private String checkoutRedirectUrl;

  @Schema(
      example =
          "https://api.platform.pagopa.it/mbd/mbd-payment/v1/00000000000/receipt/300996299640641032",
      description = "URL to download MBD receipt.")
  private String mbdDownloadLink;

  @Schema(
      example = "300996299640641032",
      description = "Notice number, used to download MBD receipt.")
  private String nav;
}
