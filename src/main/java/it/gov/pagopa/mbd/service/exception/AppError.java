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
  PAYMENT_NOTICE_REQUEST_MAP_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Error Mapping DemandPaymentNoticeRequest", "Error Mapping DemandPaymentNoticeRequest"),
  PAYMENT_NOTICE_REQUEST_CALL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Error Calling DemandPaymentNoticeRequest", "Error Calling DemandPaymentNoticeRequest"),
  CART_REQUEST_MAP_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Error Calling GetCartRequest", "Error Mapping GetCartRequest"),
  CART_REQUEST_CALL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Error Calling GetCartRequest", "Error Mapping GetCartRequest"),
  PAYMENT_RECEIPTS_CALL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Error Calling Payment Receipts", "Error Mapping GetPaymentReceipts"),

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
