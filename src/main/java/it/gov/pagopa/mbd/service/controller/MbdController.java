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
import it.gov.pagopa.mbd.service.model.mdb.GetMdbReceipt;
import it.gov.pagopa.mbd.service.service.MbdService;
import it.gov.pagopa.mbd.service.util.OpenAPIDocumentationConstants;
import jakarta.validation.ConstraintViolationException;
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

  @Operation(
      summary = "getMbd",
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
                    schema = @Schema(implementation = GetCartErrorResponse.class)))
      })
  @PostMapping(
      value = "/organizations/{fiscalCodeEC}/mbd",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<ResponseEntity> getMdb(
      @PathVariable("fiscalCodeEC") @Parameter(description = "The organization fiscal code")
          String fiscalCodeEC,
      @RequestBody GetMbdRequest request) {
    return mdbService
        .getMbd(fiscalCodeEC, request)
        .onErrorResume(
            e -> {
              if (e instanceof ConstraintViolationException) {
                return Mono.error(e);
              }
              if (e instanceof AppException) {
                return Mono.just(
                    ResponseEntity.status(((AppException) e).getHttpStatus())
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .body(
                            GetCartErrorResponse.builder()
                                .errorUrl(request.getReturnUrls().getErrorUrl())
                                .build()));
              }
              return Mono.just(
                  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                      .body(
                          GetCartErrorResponse.builder()
                              .errorUrl(request.getReturnUrls().getErrorUrl())
                              .build()));
            });
  }

  @Operation(
      summary = "getPaymentReceipt",
      description = "Return receipt of payment on requirement ",
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
      value = "/organizations/{fiscalCodeEC}/receipt/{nav}",
      produces = MediaType.APPLICATION_XML_VALUE)
  public Mono<ResponseEntity> getPaymentReceipts(
      @PathVariable("fiscalCodeEC") @Parameter(description = "The organization fiscal code")
          String fiscalCode,
      @PathVariable("nav") @Parameter(description = "The notice number") String nav) {
    return mdbService.getPaymentReceipts(fiscalCode, nav).onErrorResume(Mono::error);
  }
}
