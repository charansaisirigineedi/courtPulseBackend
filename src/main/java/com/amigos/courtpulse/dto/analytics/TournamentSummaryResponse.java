package com.amigos.courtpulse.dto.analytics;

import com.amigos.courtpulse.enums.TournamentSummaryStatusEnum;
import java.time.LocalDate;

public record TournamentSummaryResponse(
        Long tournamentId,
        String tournamentCode,
        String tournamentName,
        LocalDate tournamentDate,
        int totalMatchesPlayed,
        int totalPointsScored,
        int participantCount,
        String championPlayerCode,
        String championName,
        String runnerUpPlayerCode,
        String runnerUpName,
        TournamentSummaryStatusEnum status
) {
}
