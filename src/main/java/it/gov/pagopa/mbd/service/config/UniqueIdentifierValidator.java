package it.gov.pagopa.mbd.service.config;

import it.gov.pagopa.mbd.service.model.mdb.UniqueIdentifier;
import it.gov.pagopa.mbd.service.util.Constants;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

public class UniqueIdentifierValidator
    implements ConstraintValidator<ValidUniqueIdentifier, UniqueIdentifier> {

  @Override
  public boolean isValid(UniqueIdentifier uniqueIdentifier, ConstraintValidatorContext context) {
    if (uniqueIdentifier == null
        || uniqueIdentifier.getType() == null
        || StringUtils.isBlank(uniqueIdentifier.getValue())) {
      return buildViolation(context, "Debtor unique identifier type and value are required");
    }

    UniqueIdentifier.UniqueIdentifierType type = uniqueIdentifier.getType();
    String value = uniqueIdentifier.getValue();

    boolean valid =
        switch (type) {
          case F -> Constants.FISCAL_CODE_PATTERN.matcher(value).matches();
          case G -> Constants.VAT_NUMBER_PATTERN.matcher(value).matches();
        };

    if (!valid) {
      String message =
          type == UniqueIdentifier.UniqueIdentifierType.F
              ? "Debtor fiscal code must be a valid 16-character Italian fiscal code for type F"
              : "Debtor identifier must be an 11-digit VAT number for type G";
      return buildViolation(context, message);
    }

    return true;
  }

  private boolean buildViolation(ConstraintValidatorContext context, String message) {
    context.disableDefaultConstraintViolation();
    context
        .buildConstraintViolationWithTemplate(message)
        .addPropertyNode("uniqueIdentifier")
        .addConstraintViolation();
    return false;
  }
}
