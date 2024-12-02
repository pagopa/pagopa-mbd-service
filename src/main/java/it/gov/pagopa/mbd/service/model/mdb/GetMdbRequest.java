package it.gov.pagopa.mbd.service.model.mdb;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetMdbRequest {

    private List<PaymentNotice> paymentNotices;
    private String idCIService;
    private ReturnUrls returnUrls;

}
