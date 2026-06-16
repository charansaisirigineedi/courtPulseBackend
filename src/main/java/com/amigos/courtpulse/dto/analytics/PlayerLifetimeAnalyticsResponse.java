package com.amigos.courtpulse.dto.analytics;

import java.time.LocalDateTime;
import java.util.List;

public record PlayerLifetimeAnalyticsResponse(
        String playerCode,
        int tournamentsPlayed,
        int matchesPlayed,
        int matchesWon,
        int matchesLost,
        int matchesDrawn,
        Double winRatio,
        int singlesMatchesPlayed,
        int singlesMatchesWon,
        int singlesMatchesLost,
        int doublesMatchesPlayed,
        int doublesMatchesWon,
        int doublesMatchesLost,
        int leagueMatchesPlayed,
        int leagueMatchesWon,
        int knockoutMatchesPlayed,
        int knockoutMatchesWon,
        int totalPointsScored,
        int totalPointsConceded,
        int pointDifferential,
        Double averagePointsScoredPerMatch,
        int tournamentWins,
        int tournamentRunnerUp,
        int dominantMatchWins,
        int currentWinStreak,
        int bestWinStreak,
        LocalDateTime lastMatchAt,
        PartnerSummaryResponse bestDoublesPartner,
        List<TournamentWinPreviewResponse> recentTournamentWins
) {
}
