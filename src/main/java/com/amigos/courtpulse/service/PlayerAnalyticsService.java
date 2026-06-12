package com.amigos.courtpulse.service;

import com.amigos.courtpulse.dto.analytics.PlayerClubAnalyticsResponse;
import com.amigos.courtpulse.dto.analytics.PlayerLifetimeAnalyticsResponse;
import com.amigos.courtpulse.dto.analytics.PlayerTournamentAnalyticsResponse;
import com.amigos.courtpulse.dto.analytics.TournamentSummaryResponse;
import com.amigos.courtpulse.enums.TournamentPlacementEnum;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface PlayerAnalyticsService {

    void processMatch(Long matchId);

    PlayerLifetimeAnalyticsResponse getLifetimeAnalytics(String playerCode);

    List<PlayerTournamentAnalyticsResponse> getTournamentAnalytics(
            String playerCode,
            TournamentPlacementEnum placement,
            Pageable pageable
    );

    PlayerTournamentAnalyticsResponse getPlayerTournamentAnalytics(String playerCode, Long tournamentId);

    PlayerClubAnalyticsResponse getClubAnalytics(String playerCode, String clubCode);

    TournamentSummaryResponse getTournamentSummary(Long tournamentId);
}
