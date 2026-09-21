package it.gov.pagopa.mbd.service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AppError {
  INTERNAL_SERVER_ERROR(
      HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Something was wrong"),
  BAD_REQUEST(HttpStatus.INTERNAL_SERVER_ERROR, "Bad Request", "%s"),
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized", "Error during authentication"),
  FORBIDDEN(HttpStatus.FORBIDDEN, "Forbidden", "This method is forbidden"),
  RESPONSE_NOT_READABLE(
      HttpStatus.BAD_GATEWAY, "Response Not Readable", "The response body is not readable"),
  PAYMENT_NOTICE_REQUEST_MAP_ERROR(
      HttpStatus.INTERNAL_SERVER_ERROR,
      "Error mapping DemandPaymentNoticeRequest",
      "Error mapping DemandPaymentNoticeRequest"),
  PAYMENT_NOTICE_REQUEST_CALL_ERROR(
      HttpStatus.INTERNAL_SERVER_ERROR,
      "Error calling DemandPaymentNoticeRequest",
      "Error calling DemandPaymentNoticeRequest"),
  CART_REQUEST_MAP_ERROR(
      HttpStatus.INTERNAL_SERVER_ERROR,
      "Error mapping GetCartRequest",
      "Error mapping GetCartRequest"),
  CART_REQUEST_CALL_ERROR(
      HttpStatus.INTERNAL_SERVER_ERROR,
      "Error retrieving GetCartRequest",
      "Error calling cart API"),
  PAYMENT_RECEIPTS_NOT_FOUND(
      HttpStatus.NOT_FOUND,
      "Payment Receipts not found",
      "Error invoking GetPaymentReceipts, Payment Receipts for organization fiscal code %s and nav %s not found"),
  PAYMENT_RECEIPTS_RESPONSE_MAPPING_ERROR(
      HttpStatus.INTERNAL_SERVER_ERROR,
      "Error extracting Payment Receipts response",
      "Error mapping GetPaymentReceipts response: %s"),
  PAYMENT_RECEIPTS_CALL_ERROR(
      HttpStatus.INTERNAL_SERVER_ERROR,
      "Error invoking Payment Receipts",
      "Error in GetPaymentReceipts response: %s"),

  UNKNOWN(null, null, null);

  public final HttpStatus httpStatus;
  public final String title;
  public final String details;

  AppError(HttpStatus httpStatus, String title, String details) {
    this.httpStatus = httpStatus;
    this.title = title;
    this.details = details;
  }
}
