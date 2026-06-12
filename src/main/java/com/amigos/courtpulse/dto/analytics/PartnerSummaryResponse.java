package com.amigos.courtpulse.dto.analytics;

public record PartnerSummaryResponse(
        String playerCode,
        String name,
        String gameName,
        int matchesTogether,
        int winsTogether,
        Double winRatio
) {
}
