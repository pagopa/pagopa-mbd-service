package it.gov.pagopa.mbd.service.model.mdb;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Valid
public class GetMbdRequestV2 {

  @Size(min = 1, max = 1)
  @Valid
  private List<PaymentNoticeV2> paymentNotices;

  @Schema(
      example = "04",
      description =
          "Identifier of the association between the organization and MDB payment service.")
  @NotBlank
  private String idCIService;

  @NotNull @Valid private ReturnUrlsV2 returnUrls;
}
