package com.amigos.courtpulse.dto.analytics;

import java.time.LocalDate;

public record ClubCompletedTournamentResponse(
        Long tournamentId,
        String tournamentCode,
        String tournamentName,
        LocalDate tournamentDate,
        String championPlayerCode,
        String championName,
        int participantCount
) {
}
