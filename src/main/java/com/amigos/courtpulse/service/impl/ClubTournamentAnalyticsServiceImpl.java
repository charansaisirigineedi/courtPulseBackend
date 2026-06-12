package com.amigos.courtpulse.service.impl;

import com.amigos.courtpulse.dto.analytics.ClubCompletedTournamentResponse;
import com.amigos.courtpulse.mapper.PlayerAnalyticsMapper;
import com.amigos.courtpulse.repository.TournamentSummaryRepository;
import com.amigos.courtpulse.service.ClubTournamentAnalyticsService;
import com.amigos.courtpulse.util.AnalyticsUtil;
import com.amigos.courtpulse.util.CacheKeys;
import com.amigos.courtpulse.util.CacheNames;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClubTournamentAnalyticsServiceImpl implements ClubTournamentAnalyticsService {

    private final TournamentSummaryRepository tournamentSummaryRepository;
    private final PlayerAnalyticsMapper playerAnalyticsMapper;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheNames.CLUB_RECENT_COMPLETED_TOURNAMENTS,
            key = "T(com.amigos.courtpulse.util.CacheKeys).clubCode(#clubCode)"
    )
    public List<ClubCompletedTournamentResponse> getRecentCompletedClubTournaments(String clubCode) {
        return tournamentSummaryRepository.findRecentCompletedByClubCode(
                        CacheKeys.clubCode(clubCode),
                        PageRequest.of(0, AnalyticsUtil.recentClubCompletedTournamentsLimit())
                ).stream()
                .map(playerAnalyticsMapper::toClubCompletedTournamentResponse)
                .toList();
    }
}
