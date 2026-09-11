package it.gov.pagopa.mbd.service.controller.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.gov.pagopa.mbd.service.controller.v2.MbdControllerV2;
import it.gov.pagopa.mbd.service.exception.AppException;
import it.gov.pagopa.mbd.service.model.ProblemJson;
import it.gov.pagopa.mbd.service.model.carts.GetCartErrorResponse;
import it.gov.pagopa.mbd.service.model.carts.GetCartResponse;
import it.gov.pagopa.mbd.service.model.mdb.GetMbdRequest;
import it.gov.pagopa.mbd.service.model.mdb.GetMbdRequestV2;
import it.gov.pagopa.mbd.service.model.mdb.GetMdbReceipt;
import it.gov.pagopa.mbd.service.service.MbdService;
import it.gov.pagopa.mbd.service.util.OpenAPIDocumentationConstants;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Tag(name = "MBD - v1", description = "APIs for @eBollo v1")
@RestController
@RequestMapping("/v1")
public class MbdControllerV1 {

  private final MbdService mdbService;

  @Value("${info.application.name}")
  private String name;

  @Value("${info.application.version}")
  private String version;

  @Value("${info.properties.environment}")
  private String environment;

  public MbdControllerV1(MbdService mdbService) {
    this.mdbService = mdbService;
  }

  /**
   * @param organizationFiscalCode organization fiscal code
   * @param request request data to create the debt position and pay the Marca da Bollo
   * @return ResponseEntity containing the redirect url for payment and the redirect url for
   *     retrieving Marca da Bollo
   * @deprecated Use {@link MbdControllerV2#getMdbV2(String, GetMbdRequestV2)} instead
   *     <p>Request to pay Marca da Bollo Digitale for the provided document
   */
  @Operation(
      summary = "getMbd",
      description = "Return mbd data for payment on requirement",
      security = {@SecurityRequirement(name = "ApiKey")},
      deprecated = true)
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "OK",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = GetCartResponse.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemJson.class),
                    examples =
                        @ExampleObject(
                            value = OpenAPIDocumentationConstants.OPENAPI_BAD_REQUEST_EXAMPLE))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema())),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden",
            content = @Content(schema = @Schema())),
        @ApiResponse(
            responseCode = "429",
            description = "Too many requests",
            content = @Content(schema = @Schema())),
        @ApiResponse(
            responseCode = "500",
            description = "Service unavailable",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = GetCartErrorResponse.class)))
      })
  @PostMapping(
      value = "/organizations/{organization-fiscal-code}/mbd",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Deprecated(forRemoval = true)
  public Mono<ResponseEntity<?>> getMdb(
      @PathVariable("organization-fiscal-code") @Parameter(description = "Organization fiscal code")
          String organizationFiscalCode,
      @RequestBody GetMbdRequest request) {
    return mdbService
        .getMbd(organizationFiscalCode, request)
        .<ResponseEntity<?>>map(ResponseEntity::ok)
        .onErrorResume(
            e -> {
              if (e instanceof ConstraintViolationException) {
                return Mono.error(e);
              }
              if (e instanceof AppException appException) {
                return Mono.just(
                    ResponseEntity.status(appException.getHttpStatus())
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .body(
                            GetCartErrorResponse.builder()
                                .errorUrl(request.getReturnUrls().getErrorUrl())
                                .build()));
              }
              return Mono.just(
                  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                      .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                      .body(
                          GetCartErrorResponse.builder()
                              .errorUrl(request.getReturnUrls().getErrorUrl())
                              .build()));
            });
  }

  /**
   * @param organizationFiscalCode organization fiscal code
   * @param nav notice number
   * @return ResponseEntity containing the Marca da Bollo Digitale
   * @deprecated Use {@link MbdControllerV2#getPaymentReceiptsV2(String, String)} instead
   *     <p>Request to retrieve the Marca da Bollo receipts for the provided organization fiscal
   *     code and nav
   */
  @Operation(
      summary = "getPaymentReceipt",
      description = "Return the Marca da Bollo Digitale",
      security = {@SecurityRequirement(name = "ApiKey")},
      deprecated = true)
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "OK",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = GetMdbReceipt.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemJson.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema())),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden",
            content = @Content(schema = @Schema())),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemJson.class),
                    examples =
                        @ExampleObject(
                            value = OpenAPIDocumentationConstants.OPENAPI_NOT_FOUND_EXAMPLE))),
        @ApiResponse(
            responseCode = "429",
            description = "Too many requests",
            content = @Content(schema = @Schema())),
        @ApiResponse(
            responseCode = "500",
            description = "Service unavailable",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemJson.class),
                    examples =
                        @ExampleObject(
                            value =
                                OpenAPIDocumentationConstants
                                    .OPENAPI_INTERNAL_SERVER_ERROR_EXAMPLE))),
      })
  @GetMapping(
      value = "/organizations/{organization-fiscal-code}/receipt/{nav}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Deprecated(forRemoval = true)
  public Mono<ResponseEntity<GetMdbReceipt>> getPaymentReceipts(
      @PathVariable("organization-fiscal-code") @Parameter(description = "Organization fiscal code")
          String organizationFiscalCode,
      @PathVariable("nav") @Parameter(description = "Notice number") String nav) {
    return mdbService
        .getPaymentReceipts(organizationFiscalCode, nav)
        .map(ResponseEntity::ok)
        .onErrorResume(Mono::error);
  }
}
