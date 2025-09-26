package it.gov.pagopa.mbd.service.util;

public class OpenAPIDocumentationConstants {

  public static final String OPENAPI_BAD_REQUEST_EXAMPLE =
      """
      {
        "status": 400,
        "title": "Bad Request",
        "detail": "<detail.message>"
      }\
      """;

  public static final String OPENAPI_NOT_FOUND_EXAMPLE =
      """
      {
        "status": 404,
        "title": "Not Found",
        "detail": "<detail.message>"
      }\
      """;

  public static final String OPENAPI_INTERNAL_SERVER_ERROR_EXAMPLE =
      """
      {
        "status": 500,
        "title": "Internal Server Error",
        "detail": "<detail.message>"
      }\
      """;

  private OpenAPIDocumentationConstants() {}
}
