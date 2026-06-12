package com.amigos.courtpulse.service;

import com.amigos.courtpulse.dto.analytics.LiveMatchAnalyticsRequest;
import com.amigos.courtpulse.dto.analytics.LiveMatchAnalyticsResponse;

public interface LiveMatchAnalyticsService {

    LiveMatchAnalyticsResponse getLiveMatchAnalytics(Long tournamentId, LiveMatchAnalyticsRequest request);
}
