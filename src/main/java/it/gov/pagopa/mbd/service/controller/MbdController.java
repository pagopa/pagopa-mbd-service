package it.gov.pagopa.mbd.service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Tag(name = "MBD", description = "APIs for @eBollo")
@RestController
public class MbdController {

  private final MbdService mdbService;

  public MbdController(MbdService mdbService) {
    this.mdbService = mdbService;
  }

  /**
   * @deprecated Use {@link #getMdbV2(String, GetMbdRequestV2)} instead
   *     <p>Request to pay Marca da Bollo Digitale for the provided document
   * @param organizationFiscalCode organization fiscal code
   * @param request request data to create the debt position and pay the Marca da Bollo
   * @return ResponseEntity containing the redirect url for payment and the redirect url for
   *     retrieving Marca da Bollo
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
      value = "/v1/organizations/{organization-fiscal-code}/mbd",
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
   * Request to pay Marca da Bollo Digitale for the provided document
   *
   * @param organizationFiscalCode organization fiscal code
   * @param request request data to create the debt position and pay the Marca da Bollo
   * @return ResponseEntity containing the redirect url for payment and the redirect url for
   *     retrieving Marca da Bollo
   */
  @Operation(
      summary = "getMbdV2",
      description = "Return mbd data for payment on requirement ",
      security = {@SecurityRequirement(name = "ApiKey")})
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
      value = "/v2/organizations/{organization-fiscal-code}/mbd",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<ResponseEntity<?>> getMdbV2(
      @PathVariable("organization-fiscal-code") @Parameter(description = "Organization fiscal code")
          String organizationFiscalCode,
      @RequestBody GetMbdRequestV2 request) {
    return mdbService
        .getMbdV2(organizationFiscalCode, request)
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
   * @deprecated Use {@link #getPaymentReceipts(String, String)} instead
   *     <p>Request to retrieve the Marca da Bollo receipts for the provided organization fiscal
   *     code and nav
   * @param organizationFiscalCode organization fiscal code
   * @param nav notice number
   * @return ResponseEntity containing the Marca da Bollo Digitale
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
      value = "/v1/organizations/{organization-fiscal-code}/receipt/{nav}",
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

  /**
   * Request to retrieve the Marca da Bollo receipts for the provided organization fiscal code and
   * nav
   *
   * @param organizationFiscalCode organization fiscal code
   * @param nav notice number
   * @return ResponseEntity containing the Marca da Bollo Digitale
   */
  @Operation(
      summary = "getPaymentReceiptV2",
      description = "Return the Marca da Bollo Digitale",
      security = {@SecurityRequirement(name = "ApiKey")})
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
      value = "/v2/organizations/{organization-fiscal-code}/noticeNumbers/{nav}/mbd",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<ResponseEntity<GetMdbReceipt>> getPaymentReceiptsV2(
      @PathVariable("organization-fiscal-code") @Parameter(description = "Organization fiscal code")
          String organizationFiscalCode,
      @PathVariable("nav") @Parameter(description = "Notice number") String nav) {
    return mdbService
        .getPaymentReceipts(organizationFiscalCode, nav)
        .map(ResponseEntity::ok)
        .onErrorResume(Mono::error);
  }
}
