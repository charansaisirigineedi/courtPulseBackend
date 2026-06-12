package com.amigos.courtpulse.dto.tournament;

import com.amigos.courtpulse.enums.TournamentTypeEnum;
import com.amigos.courtpulse.validation.ValidRegistrationWindow;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;

@ValidRegistrationWindow
public record CreateTournamentRequest(
        @NotNull
        Long clubId,

        @NotBlank
        @Size(max = 100)
        String tournamentName,

        @Size(max = 500)
        String description,

        @NotNull
        TournamentTypeEnum tournamentType,

        @NotNull
        @FutureOrPresent
        LocalDate tournamentDate,

        LocalDateTime registrationStartAt,

        LocalDateTime registrationEndAt
) {
}
