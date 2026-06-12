package com.amigos.courtpulse.dto.analytics;

import java.time.LocalDateTime;
import java.util.List;

public record PlayerClubAnalyticsResponse(
        String playerCode,
        String clubCode,
        String clubName,
        int matchesPlayed,
        int matchesWon,
        int matchesLost,
        int matchesDrawn,
        Double winRatio,
        int totalPointsScored,
        int totalPointsConceded,
        int pointDifferential,
        int tournamentWins,
        int tournamentRunnerUp,
        LocalDateTime lastMatchAt,
        List<ClubCompletedTournamentResponse> recentCompletedTournaments
) {
}
