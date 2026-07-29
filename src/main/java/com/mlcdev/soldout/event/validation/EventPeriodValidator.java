package com.mlcdev.soldout.event.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EventPeriodValidator implements ConstraintValidator<ValidEventPeriod, EventPeriod> {

    @Override
    public boolean isValid(EventPeriod period, ConstraintValidatorContext context) {
        if (period == null || period.startsAt() == null || period.endsAt() == null) {
            return true;
        }
        if (period.endsAt().isAfter(period.startsAt())) {
            return true;
        }


        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode("endsAt")
                .addConstraintViolation();
        return false;
    }

}
