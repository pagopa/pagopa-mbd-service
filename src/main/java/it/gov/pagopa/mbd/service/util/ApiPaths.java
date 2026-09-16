package it.gov.pagopa.mbd.service.util;

/**
 * Centralizes REST API path templates so that the same values are used by controllers (via Spring
 * mapping annotations) and by services when they need to build absolute redirect/download URLs.
 * Placeholders use the standard Spring URI template syntax ({@code {name}}), so they can be fed
 * directly to {@code @GetMapping}/{@code @PostMapping} and to {@link
 * org.springframework.web.util.UriComponentsBuilder#buildAndExpand}.
 */
public final class ApiPaths {

  /** Base path (version prefix) of the v1 REST APIs. */
  public static final String V1_BASE = "/v1";

  /** Base path (version prefix) of the v2 REST APIs. */
  public static final String V2_BASE = "/v2";

  /** Path (relative to the version base) of the {@code POST /organizations/.../mbd} endpoint. */
  public static final String MBD_POST = "/organizations/{organization-fiscal-code}/mbd";

  /** Path (relative to {@link #V1_BASE}) of the v1 Marca da Bollo receipt download endpoint. */
  public static final String V1_MBD_RECEIPT =
      "/organizations/{organization-fiscal-code}/receipt/{nav}";

  /** Path (relative to {@link #V2_BASE}) of the v2 Marca da Bollo receipt download endpoint. */
  public static final String V2_MBD_RECEIPT =
      "/organizations/{organization-fiscal-code}/noticeNumbers/{nav}/mbd";

  private ApiPaths() {
    // utility class
  }
}
