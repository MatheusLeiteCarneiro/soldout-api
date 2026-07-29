package com.mlcdev.soldout.event.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EventPeriodValidator.class)
public @interface ValidEventPeriod {

    String message() default "{event.period.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
