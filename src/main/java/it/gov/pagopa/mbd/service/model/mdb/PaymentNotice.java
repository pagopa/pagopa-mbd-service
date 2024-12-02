package it.gov.pagopa.mbd.service.model.mdb;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentNotice {

    private String firstName;
    private String lastName;
    private String fiscalCode;
    private String email;
    private String fiscalCodeEC;
    private Long amount;
    private String province;
    private String documentHash;

}
