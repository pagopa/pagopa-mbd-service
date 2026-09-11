package it.gov.pagopa.mbd.service.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import it.gov.pagopa.mbd.service.model.AppInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.result.view.RedirectView;
import reactor.core.publisher.Mono;

@RestController
@Validated
public class HomeController {

  @Value("${info.application.name}")
  private String name;

  @Value("${info.application.version}")
  private String version;

  @Value("${info.properties.environment}")
  private String environment;

  @Value("${server.servlet.context-path}")
  String basePath;

  /**
   * @return redirect to Swagger page documentation
   */
  @Hidden
  @GetMapping("")
  public RedirectView home() {
    if (!basePath.endsWith("/")) {
      basePath += "/";
    }
    return new RedirectView(basePath + "swagger-ui.html");
  }

  /**
   * Return app name, version and environment
   *
   * @return the app info
   */
  @Operation(
      summary = "health check",
      description = "Return OK if application is started",
      security = {@SecurityRequirement(name = "ApiKey")},
      tags = {"Home"})
  @GetMapping(value = {"/v1/info", "/v2/info"})
  @ResponseStatus(HttpStatus.OK)
  public Mono<ResponseEntity<AppInfo>> healthCheck() {
    AppInfo info = AppInfo.builder().name(name).version(version).environment(environment).build();
    return Mono.just(ResponseEntity.status(HttpStatus.OK).body(info));
  }
}
