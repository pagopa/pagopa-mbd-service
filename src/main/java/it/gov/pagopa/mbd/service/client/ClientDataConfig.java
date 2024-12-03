package it.gov.pagopa.mbd.service.client;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class ClientDataConfig {

    @Value("${mbd.client.demandPayment.endpoint}")
    private String demandPaymentEndpoint;

    @Value("${mbd.client.cart.endpoint}")
    private String getCartEndpoint;

    @Value("${mbd.client.paymentReceipt.endpoint}")
    private String getPaymentReceiptEndpoint;

    @Value("${mbd.client.demandPayment.subscriptionKey}")
    private String demandPaymentSubscriptionKey;

    @Value("${mbd.client.cart.subscriptionKey}")
    private String getCartSubscriptionKey;

    @Value("${mbd.client.paymentReceipt.subscriptionKey}")
    private String getPaymentReceiptSubscriptionKey;

}
