package it.gov.pagopa.mbd.service;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
class OpenApiGenerationTest {

  @Autowired ObjectMapper objectMapper;

  @Autowired private WebTestClient webClient;

  @Test
  void swaggerSpringPlugin() {
    saveOpenAPI("/v3/api-docs", "openapi.json");
    saveOpenAPI("/v3/api-docs/v1", "openapi_v1.json");
    saveOpenAPI("/v3/api-docs/v2", "openapi_v2.json");
  }

  private void saveOpenAPI(String fromUri, String toFile) {
    webClient
        .get()
        .uri(fromUri)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody()
        .consumeWith(
            result -> {
              try {
                assertNotNull(result);
                assertNotNull(result.getResponseBodyContent());
                final String content = new String(result.getResponseBodyContent());
                assertFalse(content.isBlank());
                assertFalse(content.contains("${"), "Generated swagger contains placeholders");
                Object swagger =
                    objectMapper.readValue(
                        new String(result.getResponseBodyContent()), Object.class);
                String formatted =
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(swagger);
                Path basePath = Paths.get("openapi/");
                Files.createDirectories(basePath);
                Files.write(basePath.resolve(toFile), formatted.getBytes());
              } catch (Exception e) {
                fail();
              }
            });
  }
}
