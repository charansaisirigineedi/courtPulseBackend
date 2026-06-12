package com.amigos.courtpulse.dto.analytics;

import com.amigos.courtpulse.enums.RoundNameEnum;
import com.amigos.courtpulse.enums.TournamentPlacementEnum;
import java.time.LocalDate;

public record PlayerTournamentAnalyticsResponse(
        Long tournamentId,
        String tournamentCode,
        String tournamentName,
        LocalDate tournamentDate,
        int matchesPlayed,
        int matchesWon,
        int matchesLost,
        int matchesDrawn,
        int pointsScored,
        int pointsConceded,
        RoundNameEnum bestRoundReached,
        TournamentPlacementEnum tournamentPlacement
) {
}
