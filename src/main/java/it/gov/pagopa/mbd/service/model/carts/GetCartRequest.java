package it.gov.pagopa.mbd.service.model.carts;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetCartRequest {

  private String emailNotice;
  private List<CartPaymentNotice> paymentNotices;
  private CartReturnUrls returnUrls;
}
