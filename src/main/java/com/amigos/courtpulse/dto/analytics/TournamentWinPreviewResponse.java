package com.amigos.courtpulse.dto.analytics;

import java.time.LocalDate;

public record TournamentWinPreviewResponse(
        Long tournamentId,
        String tournamentCode,
        String tournamentName,
        LocalDate tournamentDate
) {
}
