package com.amigos.courtpulse.dto.analytics;

public record PartnershipStatsResponse(
        int matchesTogether,
        int winsTogether,
        Double winRatio
) {
}
