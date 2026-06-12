package com.amigos.courtpulse.dto.analytics;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record LiveMatchAnalyticsRequest(
        @NotEmpty(message = "Team A player codes must not be empty")
        @Size(min = 1, max = 2, message = "Team A must have 1 or 2 player codes")
        List<@NotBlank(message = "Team A player code must not be blank") String> teamAPlayerCodes,

        @NotEmpty(message = "Team B player codes must not be empty")
        @Size(min = 1, max = 2, message = "Team B must have 1 or 2 player codes")
        List<@NotBlank(message = "Team B player code must not be blank") String> teamBPlayerCodes
) {
}
