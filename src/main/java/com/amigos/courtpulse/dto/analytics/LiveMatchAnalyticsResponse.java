package com.amigos.courtpulse.dto.analytics;

import com.amigos.courtpulse.enums.TournamentTypeEnum;

public record LiveMatchAnalyticsResponse(
        Long tournamentId,
        String tournamentCode,
        TournamentTypeEnum tournamentType,
        Long clubId,
        String clubCode,
        LiveMatchTeamResponse teamA,
        LiveMatchTeamResponse teamB,
        HeadToHeadResponse headToHead
) {
}
