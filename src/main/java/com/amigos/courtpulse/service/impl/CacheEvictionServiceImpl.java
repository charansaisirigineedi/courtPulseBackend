package com.amigos.courtpulse.service.impl;

import com.amigos.courtpulse.service.CacheEvictionService;
import com.amigos.courtpulse.util.CacheKeys;
import com.amigos.courtpulse.util.CacheNames;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CacheEvictionServiceImpl implements CacheEvictionService {

    private final CacheManager cacheManager;

    @Override
    public void evictPlayerProfile(String playerCode) {
        evict(CacheNames.PLAYER_PROFILES, CacheKeys.playerCode(playerCode));
    }

    @Override
    public void evictPlayerAnalytics(String playerCode) {
        String normalizedPlayerCode = CacheKeys.playerCode(playerCode);
        evict(CacheNames.PLAYER_ANALYTICS_LIFETIME, normalizedPlayerCode);
        evict(CacheNames.PLAYER_ANALYTICS_TOURNAMENTS, normalizedPlayerCode);
    }

    @Override
    public void evictPlayerClubAnalytics(String playerCode, String clubCode) {
        evict(CacheNames.PLAYER_ANALYTICS_CLUB, CacheKeys.playerClub(playerCode, clubCode));
    }

    @Override
    public void evictTournamentSummary(Long tournamentId) {
        evict(CacheNames.TOURNAMENT_SUMMARIES, tournamentId);
    }

    @Override
    public void evictClubProfile(String clubCode) {
        evict(CacheNames.CLUB_PROFILES, CacheKeys.clubCode(clubCode));
    }

    @Override
    public void evictClubMembers(String clubCode) {
        evict(CacheNames.CLUB_MEMBERS, CacheKeys.clubCode(clubCode));
    }

    @Override
    public void evictClubTournaments(Long clubId, String clubCode) {
        evict(CacheNames.CLUB_TOURNAMENTS, String.valueOf(clubId));
        evict(CacheNames.CLUB_TOURNAMENTS, CacheKeys.clubCode(clubCode));
    }

    @Override
    public void evictClubRecentCompletedTournaments(String clubCode) {
        evict(CacheNames.CLUB_RECENT_COMPLETED_TOURNAMENTS, CacheKeys.clubCode(clubCode));
    }

    @Override
    public void evictTournamentDetails(Long tournamentId) {
        evict(CacheNames.TOURNAMENT_DETAILS, tournamentId);
    }

    @Override
    public void evictPlayerAnalyticsAfterMatch(Collection<String> playerCodes, String clubCode, Long tournamentId) {
        String normalizedClubCode = CacheKeys.clubCode(clubCode);
        for (String playerCode : playerCodes) {
            evictPlayerAnalytics(playerCode);
            evictPlayerClubAnalytics(playerCode, normalizedClubCode);
        }
        evictTournamentSummary(tournamentId);
        evictClubRecentCompletedTournaments(normalizedClubCode);
    }

    private void evict(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
        }
    }
}
