package it.gov.pagopa.mbd.service.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import it.gov.pagopa.mbd.service.model.ProblemJson;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.UnsatisfiedRequestParameterException;
import reactor.core.publisher.Mono;

/** All Exceptions are handled by this class */
@ControllerAdvice
@Slf4j
public class ErrorHandler extends ResponseEntityExceptionHandler {

  /**
   * Handle if the input request is not a valid JSON
   *
   * @param ex {@link HttpMessageNotReadableException} exception raised
   * @param headers of the response
   * @param status of the response
   * @param request from frontend
   * @return a {@link ProblemJson} as response with the cause and with a 400 as HTTP status
   */
  @ExceptionHandler({HttpMessageNotReadableException.class})

  protected Mono<ResponseEntity<Object>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status,
      ServerWebExchange request) {
    log.warn("Input not readable: ", ex);
    var errorResponse =
        ProblemJson.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .title(AppError.BAD_REQUEST.getTitle())
            .detail("Invalid input format")
            .build();
    return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST));
  }

  /**
   * Customize the response for TypeMismatchException.
   *
   * @param ex the exception
   * @param headers the headers to be written to the response
   * @param status the selected response status
   * @param request the current request
   * @return a {@code ResponseEntity} instance
   */
  @ExceptionHandler({TypeMismatchException.class})
  protected Mono<ResponseEntity<Object>> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers, HttpStatusCode status, ServerWebExchange request) {
    log.warn("Type mismatch: ", ex);
    var errorResponse =
            ProblemJson.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .title(AppError.BAD_REQUEST.getTitle())
                    .detail(
                            String.format(
                                    "Invalid value %s for property %s",
                                    ex.getValue(), ((MethodArgumentTypeMismatchException) ex).getName()))
                    .build();
    return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST));
  }

  /**
   * Handle if missing some request parameters in the request
   *
   * @param ex {@link MissingServletRequestParameterException} exception raised
   * @param headers of the response
   * @param status of the response
   * @param request from frontend
   * @return a {@link ProblemJson} as response with the cause and with a 400 as HTTP status
   */
  @Override
  protected Mono<ResponseEntity<Object>> handleUnsatisfiedRequestParameterException(UnsatisfiedRequestParameterException ex, HttpHeaders headers, HttpStatusCode status, ServerWebExchange exchange) {
    log.warn("Unsatisfied request parameter: ", ex);
    var errorResponse =
        ProblemJson.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .title(AppError.BAD_REQUEST.getTitle())
            .detail(ex.getMessage())
            .build();
    return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST));
  }


  /**
   * Handle if validation constraints are unsatisfied
   *
   * @param ex {@link MethodArgumentNotValidException} exception raised
   * @param headers of the response
   * @param status of the response
   * @param request from frontend
   * @return a {@link ProblemJson} as response with the cause and with a 400 as HTTP status
   */
  @ExceptionHandler({MethodArgumentNotValidException.class})
  protected Mono<ResponseEntity<Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status,
                                                                      ServerWebExchange request) {
    List<String> details = new ArrayList<>();
    for (FieldError error : ex.getBindingResult().getFieldErrors()) {
      details.add(error.getField() + ": " + error.getDefaultMessage());
    }
    var detailsMessage = String.join(", ", details);
    log.warn("Input not valid: " + detailsMessage);
    var errorResponse =
        ProblemJson.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .title(AppError.BAD_REQUEST.getTitle())
            .detail(detailsMessage)
            .build();
    return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST));
  }

  @ExceptionHandler({ConstraintViolationException.class})
  public Mono<ResponseEntity<ProblemJson>> handleMyException(ConstraintViolationException ex, ServerWebExchange exchange) {
    log.warn("Validation Error raised:", ex);
    var errorResponse =
        ProblemJson.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .title(AppError.BAD_REQUEST.getTitle())
            .detail(ex.getMessage())
            .build();
    return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST));
  }

  /**
   * Handle if a {@link FeignException} is raised
   *
   * @param ex {@link FeignException} exception raised
   * @param request from frontend
   * @return a {@link ProblemJson} as response with the cause and with an appropriated HTTP status
   */
  @ExceptionHandler({FeignException.class})
  public Mono<ResponseEntity<ProblemJson>> handleFeignException(
      final FeignException ex, final ServerWebExchange request) {
    log.warn("FeignException raised: ", ex);

    ProblemJson problem;
    if (ex.responseBody().isPresent()) {
      var body = new String(ex.responseBody().get().array(), StandardCharsets.UTF_8);
      try {
        problem = new ObjectMapper().readValue(body, ProblemJson.class);
      } catch (JsonProcessingException e) {
        problem =
            ProblemJson.builder()
                .status(HttpStatus.BAD_GATEWAY.value())
                .title(AppError.RESPONSE_NOT_READABLE.getTitle())
                .detail(AppError.RESPONSE_NOT_READABLE.getDetails())
                .build();
      }
    } else {
      problem =
          ProblemJson.builder()
              .status(HttpStatus.BAD_GATEWAY.value())
              .title("No Response Body")
              .detail("Error with external dependency")
              .build();
    }

    return Mono.just(new ResponseEntity<>(problem, HttpStatus.valueOf(problem.getStatus())));
  }

  /**
   * Handle if a {@link AppException} is raised
   *
   * @param ex {@link AppException} exception raised
   * @param request from frontend
   * @return a {@link ProblemJson} as response with the cause and with an appropriated HTTP status
   */
  @ExceptionHandler({AppException.class})
  public ResponseEntity<ProblemJson> handleAppException(
      final AppException ex, final WebRequest request) {
    if (ex.getCause() != null) {
      log.warn(
          "App Exception raised: " + ex.getMessage() + "\nCause of the App Exception: ",
          ex.getCause());
    } else {
      log.warn("App Exception raised: ", ex);
    }
    var errorResponse =
        ProblemJson.builder()
            .status(ex.getHttpStatus().value())
            .title(ex.getTitle())
            .detail(ex.getMessage())
            .build();
    return new ResponseEntity<>(errorResponse, ex.getHttpStatus());
  }

  /**
   * Handle if a {@link Exception} is raised
   *
   * @param ex {@link Exception} exception raised
   * @param request from frontend
   * @return a {@link ProblemJson} as response with the cause and with 500 as HTTP status
   */
  @ExceptionHandler({Exception.class})
  public Mono<ResponseEntity<ProblemJson>> handleGenericException(
      final Exception ex, final ServerWebExchange request) {
    log.error("Generic Exception raised:", ex);
    var errorResponse =
        ProblemJson.builder()
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .title(AppError.INTERNAL_SERVER_ERROR.getTitle())
            .detail(ex.getMessage())
            .build();
    return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR));
  }
}
