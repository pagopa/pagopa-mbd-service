package it.gov.pagopa.mbd.service.config;

import it.gov.pagopa.mbd.service.model.mdb.UniqueIdentifier;
import it.gov.pagopa.mbd.service.model.mdb.UniqueIdentifier.UniqueIdentifierType;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Validates the {@link UniqueIdentifier} of a debtor: it must be present, and its value must match
 * the format expected for the declared {@code uniqueIdentifier.type} (16-character Italian fiscal
 * code for {@link UniqueIdentifierType#F}, 11-digit VAT number for {@link UniqueIdentifierType#G}).
 */
@Documented
@Constraint(validatedBy = UniqueIdentifierValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUniqueIdentifier {

  String message() default "Invalid debtor unique identifier";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
