package com.amigos.courtpulse.validation;

import com.amigos.courtpulse.dto.tournament.CreateTournamentRequest;
import com.amigos.courtpulse.util.ObjectUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RegistrationWindowValidator implements ConstraintValidator<ValidRegistrationWindow, CreateTournamentRequest> {

    @Override
    public boolean isValid(CreateTournamentRequest request, ConstraintValidatorContext context) {
        if (ObjectUtil.isNull(request)
                || ObjectUtil.isNull(request.registrationStartAt())
                || ObjectUtil.isNull(request.registrationEndAt())) {
            return true;
        }

        return request.registrationEndAt().isAfter(request.registrationStartAt());
    }
}
