package it.gov.pagopa.mbd.service.util;

import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {

  public static final String HEADER_REQUEST_ID = "X-Request-Id";

  private static final String START_REGEX = "^";
  private static final String END_REGEX = "$";
  private static final String FISCAL_CODE_REGEX = "[A-Z]{6}\\d{2}[A-Z]\\d{2}[A-Z]\\d{3}[A-Z]";
  private static final String VAT_NUMBER_REGEX = "\\d{11}";
  public static final String GENERIC_FISCAL_CODE_REGEX =
      START_REGEX + "(?:" + FISCAL_CODE_REGEX + "|" + VAT_NUMBER_REGEX + ")" + END_REGEX;
  public static final Pattern FISCAL_CODE_PATTERN =
      Pattern.compile(START_REGEX + FISCAL_CODE_REGEX + END_REGEX);
  public static final Pattern VAT_NUMBER_PATTERN =
      Pattern.compile(START_REGEX + VAT_NUMBER_REGEX + END_REGEX);
}
