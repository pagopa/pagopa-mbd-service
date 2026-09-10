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

import java.util.*;

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
      @Value("${info.application.version}") String appVersion) {
    return new OpenAPI()
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
                .title("EBollo 2.0 - Service for partner")
                .version(appVersion)
                .description(appDescription)
                .termsOfService("https://www.pagopa.gov.it/"));
  }

  @Bean
  public GroupedOpenApi apiV1() {
    List<Server> serverInfo = new ArrayList<>();

    serverInfo.add(createServer(".uat", "pagopa-mbd-service", "v1", "EBollo 2.0 Test environment"));
    serverInfo.add(createServer("", "pagopa-mbd-service", "v1", "EBollo 2.0 Production Environment"));

    return GroupedOpenApi.builder()
        .group("v1")
        .displayName("Marca da Bollo Digitale v1")
        .pathsToMatch("/v1/**")
        .addOpenApiCustomizer(removeVersionFromPaths("/v1"))
        .addOpenApiCustomizer(customizeServer(serverInfo))
        .build();
  }

  @Bean
  public GroupedOpenApi apiV2() {
    List<Server> serverInfo = new ArrayList<>();

    serverInfo.add(createServer(".uat", "pagopa-mbd-service", "v2", "EBollo 2.0 Test environment"));
    serverInfo.add(createServer("", "pagopa-mbd-service", "v2", "EBollo 2.0 Production Environment"));
    return GroupedOpenApi.builder()
        .group("v2")
        .displayName("Marca da Bollo Digitale v2")
        .pathsToMatch("/v2/**")
        .addOpenApiCustomizer(removeVersionFromPaths("/v2"))
        .addOpenApiCustomizer(customizeServer(serverInfo))
        .build();
  }

  /**
   * Removes the version prefix (e.g. "/v1") from the documented paths and instead appends it to
   * the server URLs, so that the path shown in the swagger does not contain the version, while
   * the server url does.
   */
  private OpenApiCustomizer removeVersionFromPaths(String versionPrefix) {
    return openApi -> {
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

      Optional.ofNullable(openApi.getServers())
          .orElse(Collections.emptyList())
          .forEach(
              server -> {
                if (server.getUrl() != null) {
                  server.setUrl(server.getUrl() + versionPrefix);
                }
                Optional.ofNullable(server.getVariables())
                    .map(variables -> variables.get("basePath"))
                    .ifPresent(
                        basePath -> basePath.setDefault(basePath.getDefault() + versionPrefix));
              });
    };
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

    private Server createServer(String env, String service, String version, String description) {
        String baseUrl = "https://api%s.platform.pagopa.it/%s";
        String url = String.format(baseUrl, env, service);
        if (version != null) {
            url = String.format("%s/%s", url, version);
        }
        Server server = new Server();
        server.setUrl(url);
        server.setDescription(description);
        return server;
    }

    private OpenApiCustomizer customizeServer(List<Server> serverInfo) {
        return openApi -> {
            if (openApi.getPaths() == null) return;

            // set servers
            openApi.setServers(serverInfo);
        };
    }
}
