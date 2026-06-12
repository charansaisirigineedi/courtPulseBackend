package com.amigos.courtpulse.util;

import java.util.Locale;
import org.springframework.util.StringUtils;

public final class CacheKeys {

    private CacheKeys() {
    }

    public static String playerCode(String playerCode) {
        return playerCode.trim().toUpperCase(Locale.ROOT);
    }

    public static String clubCode(String clubCode) {
        return clubCode.trim().toUpperCase(Locale.ROOT);
    }

    public static String clubIdentifier(String clubIdentifier) {
        String normalized = clubIdentifier.trim();
        if (normalized.matches("^\\d+$")) {
            return normalized;
        }
        return normalized.toUpperCase(Locale.ROOT);
    }

    public static String playerClub(String playerCode, String clubCode) {
        return playerCode(playerCode) + ":" + clubCode(clubCode);
    }

    public static String playerTournamentAnalytics(
            String playerCode,
            String placementKey,
            int pageSize,
            int pageNumber
    ) {
        return playerCode(playerCode) + ":" + placementKey + ":" + pageSize + ":" + pageNumber;
    }

    public static String searchQuery(String query) {
        return StringUtils.hasText(query) ? query.trim().toLowerCase(Locale.ROOT) : "";
    }
}
