package com.amigos.courtpulse.dto.analytics;

import com.amigos.courtpulse.enums.RoundNameEnum;
import com.amigos.courtpulse.enums.TournamentPlacementEnum;

public record LiveMatchPlayerResponse(
        String playerCode,
        String name,
        String gameName,
        int matchesPlayed,
        int matchesWon,
        int matchesLost,
        int matchesDrawn,
        int pointsScored,
        int pointsConceded,
        Double winRatio,
        RoundNameEnum bestRoundReached,
        TournamentPlacementEnum tournamentPlacement
) {
}
