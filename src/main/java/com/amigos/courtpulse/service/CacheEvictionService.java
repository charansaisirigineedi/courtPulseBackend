package com.amigos.courtpulse.service;

import java.util.Collection;

public interface CacheEvictionService {

    void evictPlayerProfile(String playerCode);

    void evictPlayerAnalytics(String playerCode);

    void evictPlayerClubAnalytics(String playerCode, String clubCode);

    void evictTournamentSummary(Long tournamentId);

    void evictClubProfile(String clubCode);

    void evictClubMembers(String clubCode);

    void evictClubTournaments(Long clubId, String clubCode);

    void evictClubRecentCompletedTournaments(String clubCode);

    void evictTournamentDetails(Long tournamentId);

    void evictPlayerAnalyticsAfterMatch(Collection<String> playerCodes, String clubCode, Long tournamentId);
}
