package com.amigos.courtpulse.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = RegistrationWindowValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidRegistrationWindow {

    String message() default "registrationEndAt must be after registrationStartAt";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
