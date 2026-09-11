package it.gov.pagopa.mbd.service.model.carts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartReturnUrlsV2 {

  private String returnOkUrl;
  private String returnCancelUrl;
  private String returnErrorUrl;
  private String returnWaitingUrl;
}
