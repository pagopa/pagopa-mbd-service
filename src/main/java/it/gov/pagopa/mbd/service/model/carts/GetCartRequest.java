package it.gov.pagopa.mbd.service.model.carts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetCartRequest {

    private String emailNotice;
    private List<CartPaymentNotice> paymentNotices;
    private ReturnUrls returnUrls;


}
