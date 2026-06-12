package com.amigos.courtpulse.service;

import com.amigos.courtpulse.dto.analytics.ClubCompletedTournamentResponse;
import java.util.List;

public interface ClubTournamentAnalyticsService {

    List<ClubCompletedTournamentResponse> getRecentCompletedClubTournaments(String clubCode);
}
