package it.gov.pagopa.mbd.service.config;

import static it.gov.pagopa.mbd.service.util.Constants.HEADER_REQUEST_ID;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.servers.ServerVariable;
import io.swagger.v3.oas.models.servers.ServerVariables;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  public static final String BASE_PATH = "/pagopa-mbd-service";

  @Bean
  public OpenAPI customOpenAPI(
      @Value("${info.application.name}") String appName,
      @Value("${info.application.description}") String appDescription,
      @Value("${info.application.version}") String appVersion,
      @Value("${server.port}") String serverPort) {
    return new OpenAPI()
        .servers(
            createServers(
                serverPort, new ServerVariable()._enum(List.of("/v1", "/v2"))._default("/v1")))
        .components(
            new Components()
                .addSecuritySchemes(
                    "ApiKey",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .description("The API key to access this function app.")
                        .name("Ocp-Apim-Subscription-Key")
                        .in(SecurityScheme.In.HEADER)))
        .info(
            new Info()
                .title(appName)
                .version(appVersion)
                .description(appDescription)
                .termsOfService("https://www.pagopa.gov.it/"));
  }

  @Bean
  public OpenApiCustomizer sortOperationsAlphabetically() {
    return openApi -> {
      Paths paths =
          openApi.getPaths().entrySet().stream()
              .sorted(Map.Entry.comparingByKey())
              .collect(
                  Paths::new,
                  (map, item) -> map.addPathItem(item.getKey(), item.getValue()),
                  Paths::putAll);

      paths.forEach(
          (key, value) ->
              value
                  .readOperations()
                  .forEach(
                      operation -> {
                        var responses =
                            operation.getResponses().entrySet().stream()
                                .sorted(Map.Entry.comparingByKey())
                                .collect(
                                    ApiResponses::new,
                                    (map, item) ->
                                        map.addApiResponse(item.getKey(), item.getValue()),
                                    ApiResponses::putAll);
                        operation.setResponses(responses);
                      }));
      openApi.setPaths(paths);
    };
  }

  @Bean
  public OpenApiCustomizer addCommonHeaders() {
    return openApi ->
        openApi
            .getPaths()
            .forEach(
                (key, value) -> {

                  // add Request-ID as request header
                  var header =
                      Optional.ofNullable(value.getParameters())
                          .orElse(Collections.emptyList())
                          .parallelStream()
                          .filter(Objects::nonNull)
                          .anyMatch(elem -> HEADER_REQUEST_ID.equals(elem.getName()));
                  if (!header) {
                    value.addParametersItem(
                        new Parameter()
                            .in("header")
                            .name(HEADER_REQUEST_ID)
                            .schema(new StringSchema())
                            .description(
                                "This header identifies the call, if not passed it is"
                                    + " self-generated. This ID is returned in the response."));
                  }
                  // add Request-ID as response header
                  value
                      .readOperations()
                      .forEach(
                          operation ->
                              operation
                                  .getResponses()
                                  .values()
                                  .forEach(
                                      response ->
                                          response.addHeaderObject(
                                              HEADER_REQUEST_ID,
                                              new Header()
                                                  .schema(new StringSchema())
                                                  .description(
                                                      "This header identifies the call"))));
                });
  }

  @Bean
  public Map<String, GroupedOpenApi> configureGroupOpenApi(
      Map<String, GroupedOpenApi> groupOpenApi, @Value("${server.port}") String serverPort) {
    groupOpenApi.forEach(
        (id, groupedOpenApi) ->
            groupedOpenApi
                .getOpenApiCustomizers()
                .add(
                    openApi -> {
                      if (id.equals("v1")) {
                        openApi.getInfo().setDescription("Marca da Bollo Digitale v1");
                        openApi.setServers(
                            createServers(
                                serverPort,
                                new ServerVariable()._enum(List.of("/v1"))._default("/v1")));
                        removeVersionFromPaths(openApi, "/v1");
                      } else if (id.equals("v2")) {
                        openApi.getInfo().setDescription("Marca da Bollo Digitale v2");
                        openApi.setServers(
                            createServers(
                                serverPort,
                                new ServerVariable()._enum(List.of("/v2"))._default("/v2")));
                        removeVersionFromPaths(openApi, "/v2");
                      }
                    }));
    return groupOpenApi;
  }

  /**
   * Removes the version prefix (e.g. "/v1") from the documented paths and instead appends it to the
   * server URLs, so that the path shown in the swagger does not contain the version, while the
   * server url does.
   */
  private void removeVersionFromPaths(OpenAPI openApi, String versionPrefix) {
    Paths oldPaths = openApi.getPaths();
    if (oldPaths != null) {
      Paths newPaths = new Paths();
      oldPaths.forEach(
          (path, pathItem) -> {
            String newPath =
                path.startsWith(versionPrefix) ? path.substring(versionPrefix.length()) : path;
            if (newPath.isEmpty()) {
              newPath = "/";
            }
            newPaths.addPathItem(newPath, pathItem);
          });
      openApi.setPaths(newPaths);
    }
  }

  private static @NonNull List<Server> createServers(String serverPort, ServerVariable version) {
    String localPath = String.format("%s://%s:%s", "http", "localhost", serverPort);
    return List.of(
        new Server().url(localPath),
        new Server()
            .url("https://{host}{basePath}{version}")
            .variables(
                new ServerVariables()
                    .addServerVariable(
                        "host",
                        new ServerVariable()
                            ._enum(
                                List.of(
                                    "api.dev.platform.pagopa.it",
                                    "api.uat.platform.pagopa.it",
                                    "api.platform.pagopa.it"))
                            ._default("api.dev.platform.pagopa.it"))
                    .addServerVariable("basePath", new ServerVariable()._default(BASE_PATH))
                    .addServerVariable("version", version)));
  }
}
