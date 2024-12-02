package it.gov.pagopa.mbd.service.service.impl;

import it.gov.pagopa.mbd.service.client.ReactiveClient;
import it.gov.pagopa.mbd.service.controller.MbdController;
import it.gov.pagopa.mbd.service.model.xml.node.nodeforpsp.DemandPaymentNoticeResponse;
import it.gov.pagopa.mbd.service.service.MdbService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class MdbServiceImplTest {

    @MockBean
    private ReactiveClient reactiveClient;

    @Test
    void getMdbShouldReturnResponseEntityOnValidData() {

        when(reactiveClient.demandPaymentNotice(any())).thenAnswer(
                item -> Mono.just(DemandPaymentNoticeResponse.builder().build())
        );


    }

    @Test
    void getPaymentReceipts() {
    }
}