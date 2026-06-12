package com.amigos.courtpulse.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AnalyticsUtilTest {

    @Test
    void clampTournamentHistoryLimitUsesDefaultForNonPositiveValues() {
        assertEquals(AnalyticsUtil.defaultTournamentHistoryLimit(), AnalyticsUtil.clampTournamentHistoryLimit(0));
        assertEquals(AnalyticsUtil.defaultTournamentHistoryLimit(), AnalyticsUtil.clampTournamentHistoryLimit(-5));
    }

    @Test
    void clampTournamentHistoryLimitCapsAtMax() {
        assertEquals(AnalyticsUtil.maxTournamentHistoryLimit(), AnalyticsUtil.clampTournamentHistoryLimit(100));
        assertEquals(10, AnalyticsUtil.clampTournamentHistoryLimit(10));
    }
}
