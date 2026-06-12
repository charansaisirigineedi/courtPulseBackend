package com.amigos.courtpulse.dto.analytics;

import java.util.List;

public record LiveMatchTeamResponse(
        List<LiveMatchPlayerResponse> players,
        PartnershipStatsResponse partnership
) {
}
