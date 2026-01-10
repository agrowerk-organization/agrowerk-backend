package tech.agrowerk.business.validators;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DateRangeValidator.class)
public @interface ValidDateRange {
    String message() default "The end date must be later than the start date.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    String startDateField() default "startDate";
    String endDateField() default "endDate";
}
