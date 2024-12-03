package it.gov.pagopa.mbd.service.model.carts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartPaymentNotice {

    private String noticeNumber;
    private String fiscalCode;
    private Long amount;
    private String companyName;
    private String description;

}
