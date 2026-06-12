package com.amigos.courtpulse.dto.analytics;

public record HeadToHeadResponse(
        int matchesPlayed,
        int teamAWins,
        int teamBWins,
        int draws
) {
}
