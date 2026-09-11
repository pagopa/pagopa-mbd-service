package it.gov.pagopa.mbd.service.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = HomeController.class)
class HomeControllerTest {

  @Autowired private WebTestClient webClient;

  @ParameterizedTest
  @ValueSource(strings = {"/v1/info", "/v2/info"})
  void healthCheckTestSuccess(String infoEndpoint) {
    webClient.get().uri(infoEndpoint).exchange().expectStatus().is2xxSuccessful();
  }

  @Test
  void homeTestSuccess() {
    webClient.get().exchange().expectStatus().is2xxSuccessful();
  }
}
