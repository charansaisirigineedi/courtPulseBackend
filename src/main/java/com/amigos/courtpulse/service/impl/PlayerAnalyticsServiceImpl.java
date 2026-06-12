package com.amigos.courtpulse.service.impl;

import com.amigos.courtpulse.dto.analytics.ClubCompletedTournamentResponse;
import com.amigos.courtpulse.dto.analytics.PartnerSummaryResponse;
import com.amigos.courtpulse.dto.analytics.PlayerClubAnalyticsResponse;
import com.amigos.courtpulse.dto.analytics.PlayerLifetimeAnalyticsResponse;
import com.amigos.courtpulse.dto.analytics.PlayerTournamentAnalyticsResponse;
import com.amigos.courtpulse.dto.analytics.TournamentSummaryResponse;
import com.amigos.courtpulse.entity.Club;
import com.amigos.courtpulse.entity.Player;
import com.amigos.courtpulse.entity.PlayerClubStats;
import com.amigos.courtpulse.entity.PlayerLifetimeStats;
import com.amigos.courtpulse.entity.PlayerTournamentStats;
import com.amigos.courtpulse.entity.TournamentMatch;
import com.amigos.courtpulse.entity.TournamentMatchPlayer;
import com.amigos.courtpulse.entity.TournamentMetadata;
import com.amigos.courtpulse.entity.TournamentSummary;
import com.amigos.courtpulse.enums.PlayerSideEnum;
import com.amigos.courtpulse.enums.RoundNameEnum;
import com.amigos.courtpulse.enums.TournamentPlacementEnum;
import com.amigos.courtpulse.enums.TournamentStatusEnum;
import com.amigos.courtpulse.enums.TournamentSummaryStatusEnum;
import com.amigos.courtpulse.enums.TournamentTypeEnum;
import com.amigos.courtpulse.enums.WinnerSideEnum;
import com.amigos.courtpulse.exception.ClubNotFoundException;
import com.amigos.courtpulse.exception.PlayerNotFoundException;
import com.amigos.courtpulse.exception.TournamentNotFoundException;
import com.amigos.courtpulse.mapper.PlayerAnalyticsMapper;
import com.amigos.courtpulse.repository.ClubRepository;
import com.amigos.courtpulse.repository.PlayerClubStatsRepository;
import com.amigos.courtpulse.repository.PlayerLifetimeStatsRepository;
import com.amigos.courtpulse.repository.PlayerRepository;
import com.amigos.courtpulse.repository.PlayerTournamentStatsRepository;
import com.amigos.courtpulse.repository.TournamentMatchPlayerRepository;
import com.amigos.courtpulse.repository.TournamentMatchRepository;
import com.amigos.courtpulse.repository.TournamentMetadataRepository;
import com.amigos.courtpulse.repository.TournamentSummaryRepository;
import com.amigos.courtpulse.service.CacheEvictionService;
import com.amigos.courtpulse.service.ClubTournamentAnalyticsService;
import com.amigos.courtpulse.service.PlayerAnalyticsService;
import com.amigos.courtpulse.util.AnalyticsUtil;
import com.amigos.courtpulse.util.CacheKeys;
import com.amigos.courtpulse.util.CacheNames;
import com.amigos.courtpulse.util.ObjectUtil;
import com.amigos.courtpulse.util.PaginationUtil;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlayerAnalyticsServiceImpl implements PlayerAnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(PlayerAnalyticsServiceImpl.class);

    private final TournamentMatchRepository tournamentMatchRepository;
    private final TournamentMatchPlayerRepository tournamentMatchPlayerRepository;
    private final PlayerLifetimeStatsRepository playerLifetimeStatsRepository;
    private final PlayerTournamentStatsRepository playerTournamentStatsRepository;
    private final PlayerClubStatsRepository playerClubStatsRepository;
    private final TournamentSummaryRepository tournamentSummaryRepository;
    private final PlayerRepository playerRepository;
    private final ClubRepository clubRepository;
    private final TournamentMetadataRepository tournamentMetadataRepository;
    private final PlayerAnalyticsMapper playerAnalyticsMapper;
    private final CacheEvictionService cacheEvictionService;
    private final ClubTournamentAnalyticsService clubTournamentAnalyticsService;

    @Override
    @Transactional
    public void processMatch(Long matchId) {
        TournamentMatch match = tournamentMatchRepository.findByIdWithTournament(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found for analytics processing: " + matchId));

        if (ObjectUtil.nonNull(match.getAnalyticsProcessedAt())) {
            log.warn("Skipping analytics processing for match ID {} because it was already processed at {}",
                    matchId, match.getAnalyticsProcessedAt());
            return;
        }

        List<TournamentMatchPlayer> matchPlayers = tournamentMatchPlayerRepository.findByMatchIdWithPlayer(matchId);
        TournamentMetadata tournament = match.getTournament();
        Club club = tournament.getClub();
        boolean doublesTournament = AnalyticsUtil.isDoublesTournament(tournament.getTournamentType());
        boolean knockoutRound = AnalyticsUtil.isKnockoutRound(match.getRoundName());
        boolean firstParticipationInTournament = false;

        TournamentSummary tournamentSummary = findOrCreateTournamentSummary(tournament);
        tournamentSummary.setTotalMatchesPlayed(tournamentSummary.getTotalMatchesPlayed() + 1);
        tournamentSummary.setTotalPointsScored(tournamentSummary.getTotalPointsScored() + match.getScoreA() + match.getScoreB());

        for (TournamentMatchPlayer matchPlayer : matchPlayers) {
            Player player = matchPlayer.getPlayer();
            int pointsScored = resolvePointsScored(match, matchPlayer.getSide());
            int pointsConceded = resolvePointsConceded(match, matchPlayer.getSide());
            MatchOutcome outcome = resolveOutcome(match, matchPlayer.isWinner());

            PlayerLifetimeStats lifetimeStats = findOrCreateLifetimeStats(player);
            PlayerTournamentStats tournamentStats = findOrCreateTournamentStats(player, tournament);
            PlayerClubStats clubStats = findOrCreateClubStats(player, club);

            if (tournamentStats.getMatchesPlayed() == 0) {
                firstParticipationInTournament = true;
                tournamentSummary.setParticipantCount(tournamentSummary.getParticipantCount() + 1);
            }

            applyMatchCounters(lifetimeStats, outcome, pointsScored, pointsConceded, doublesTournament, match);
            applyMatchCountersToTournamentStats(tournamentStats, outcome, pointsScored, pointsConceded);
            applyMatchCountersToClubStats(clubStats, outcome, pointsScored, pointsConceded);

            updateStreaks(lifetimeStats, outcome, match.getCompletedAt());
            clubStats.setLastMatchAt(match.getCompletedAt());

            if (knockoutRound) {
                updateBestRoundReached(tournamentStats, match.getRoundName());
                updateKnockoutPlacement(tournamentStats, match, matchPlayer);
            }

            playerLifetimeStatsRepository.save(lifetimeStats);
            playerTournamentStatsRepository.save(tournamentStats);
            playerClubStatsRepository.save(clubStats);
        }

        boolean finalMatch = ObjectUtil.isEqual(match.getRoundName(), RoundNameEnum.FINAL);
        if (finalMatch) {
            applyFinalOutcome(matchPlayers, tournamentSummary);
            tournament.setStatus(TournamentStatusEnum.COMPLETED);
            tournamentMetadataRepository.save(tournament);
        }

        tournamentSummaryRepository.save(tournamentSummary);
        match.setAnalyticsProcessedAt(LocalDateTime.now());
        tournamentMatchRepository.save(match);

        List<String> affectedPlayerCodes = matchPlayers.stream()
                .map(matchPlayer -> matchPlayer.getPlayer().getPlayerCode())
                .toList();
        cacheEvictionService.evictPlayerAnalyticsAfterMatch(
                affectedPlayerCodes,
                club.getClubCode(),
                tournament.getId()
        );
        if (finalMatch) {
            cacheEvictionService.evictTournamentDetails(tournament.getId());
            cacheEvictionService.evictClubTournaments(club.getId(), club.getClubCode());
            log.info("Tournament ID: {} marked COMPLETED after final match ID: {}", tournament.getId(), matchId);
        }

        log.info("Processed analytics for match ID {} in tournament ID {}{}",
                matchId,
                tournament.getId(),
                firstParticipationInTournament ? " with new participants" : "");
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheNames.PLAYER_ANALYTICS_LIFETIME,
            key = "T(com.amigos.courtpulse.util.CacheKeys).playerCode(#playerCode)"
    )
    public PlayerLifetimeAnalyticsResponse getLifetimeAnalytics(String playerCode) {
        Player player = playerRepository.findByPlayerCode(playerCode)
                .orElseThrow(() -> new PlayerNotFoundException(playerCode));

        PlayerLifetimeStats stats = playerLifetimeStatsRepository.findByPlayer_PlayerCode(playerCode).orElse(null);
        PartnerSummaryResponse bestPartner = findBestDoublesPartner(player.getId());
        var recentWins = playerTournamentStatsRepository.findRecentWinsByPlayerCode(
                        playerCode,
                        PageRequest.of(0, AnalyticsUtil.recentTournamentWinsPreviewLimit())
                ).stream()
                .map(playerAnalyticsMapper::toTournamentWinPreview)
                .toList();
        return playerAnalyticsMapper.toLifetimeResponse(player, stats, bestPartner, recentWins);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheNames.PLAYER_ANALYTICS_TOURNAMENTS,
            key = "T(com.amigos.courtpulse.util.CacheKeys).playerTournamentAnalytics(#playerCode, "
                    + "(#placement == null ? 'ALL' : #placement.name()), "
                    + "#pageable.pageSize, #pageable.pageNumber)"
    )
    public List<PlayerTournamentAnalyticsResponse> getTournamentAnalytics(
            String playerCode,
            TournamentPlacementEnum placement,
            Pageable pageable
    ) {
        if (!playerRepository.existsByPlayerCode(playerCode)) {
            throw new PlayerNotFoundException(playerCode);
        }

        Pageable normalizedPageable = PaginationUtil.normalize(
                pageable,
                AnalyticsUtil.defaultTournamentHistoryLimit(),
                AnalyticsUtil.maxTournamentHistoryLimit()
        );

        return playerTournamentStatsRepository.findByPlayerCodeWithTournament(
                        playerCode,
                        placement,
                        normalizedPageable
                ).stream()
                .map(playerAnalyticsMapper::toTournamentResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PlayerTournamentAnalyticsResponse getPlayerTournamentAnalytics(String playerCode, Long tournamentId) {
        if (!playerRepository.existsByPlayerCode(playerCode)) {
            throw new PlayerNotFoundException(playerCode);
        }
        if (!tournamentMetadataRepository.existsById(tournamentId)) {
            throw new TournamentNotFoundException(tournamentId);
        }

        PlayerTournamentStats stats = playerTournamentStatsRepository
                .findByPlayerCodeAndTournamentId(playerCode, tournamentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No tournament analytics found for player " + playerCode + " in tournament " + tournamentId));

        return playerAnalyticsMapper.toTournamentResponse(stats);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheNames.PLAYER_ANALYTICS_CLUB,
            key = "T(com.amigos.courtpulse.util.CacheKeys).playerClub(#playerCode, #clubCode)"
    )
    public PlayerClubAnalyticsResponse getClubAnalytics(String playerCode, String clubCode) {
        if (!playerRepository.existsByPlayerCode(playerCode)) {
            throw new PlayerNotFoundException(playerCode);
        }
        if (!clubRepository.existsByClubCode(clubCode)) {
            throw new ClubNotFoundException(clubCode);
        }

        PlayerClubStats stats = playerClubStatsRepository
                .findByPlayer_PlayerCodeAndClub_ClubCode(playerCode, clubCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No club analytics found for player " + playerCode + " in club " + clubCode));

        List<ClubCompletedTournamentResponse> recentCompletedTournaments =
                clubTournamentAnalyticsService.getRecentCompletedClubTournaments(clubCode);

        return playerAnalyticsMapper.toClubResponse(stats, recentCompletedTournaments);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.TOURNAMENT_SUMMARIES, key = "#tournamentId")
    public TournamentSummaryResponse getTournamentSummary(Long tournamentId) {
        if (!tournamentMetadataRepository.existsById(tournamentId)) {
            throw new TournamentNotFoundException(tournamentId);
        }

        TournamentSummary summary = tournamentSummaryRepository.findByTournamentIdWithDetails(tournamentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No analytics summary found for tournament ID " + tournamentId));

        return playerAnalyticsMapper.toTournamentSummaryResponse(summary);
    }

    private PlayerLifetimeStats findOrCreateLifetimeStats(Player player) {
        return playerLifetimeStatsRepository.findByPlayerIdForUpdate(player.getId())
                .orElseGet(() -> playerLifetimeStatsRepository.save(
                        PlayerLifetimeStats.builder().player(player).build()
                ));
    }

    private PlayerTournamentStats findOrCreateTournamentStats(Player player, TournamentMetadata tournament) {
        return playerTournamentStatsRepository.findByPlayerIdAndTournamentIdForUpdate(player.getId(), tournament.getId())
                .orElseGet(() -> playerTournamentStatsRepository.save(
                        PlayerTournamentStats.builder()
                                .player(player)
                                .tournament(tournament)
                                .tournamentPlacement(TournamentPlacementEnum.PARTICIPANT)
                                .build()
                ));
    }

    private PlayerClubStats findOrCreateClubStats(Player player, Club club) {
        return playerClubStatsRepository.findByPlayerIdAndClubIdForUpdate(player.getId(), club.getId())
                .orElseGet(() -> playerClubStatsRepository.save(
                        PlayerClubStats.builder()
                                .player(player)
                                .club(club)
                                .build()
                ));
    }

    private TournamentSummary findOrCreateTournamentSummary(TournamentMetadata tournament) {
        return tournamentSummaryRepository.findByTournamentIdForUpdate(tournament.getId())
                .orElseGet(() -> tournamentSummaryRepository.save(
                        TournamentSummary.builder()
                                .tournament(tournament)
                                .status(TournamentSummaryStatusEnum.IN_PROGRESS)
                                .build()
                ));
    }

    private void applyMatchCounters(
            PlayerLifetimeStats stats,
            MatchOutcome outcome,
            int pointsScored,
            int pointsConceded,
            boolean doublesTournament,
            TournamentMatch match
    ) {
        stats.setMatchesPlayed(stats.getMatchesPlayed() + 1);
        stats.setTotalPointsScored(stats.getTotalPointsScored() + pointsScored);
        stats.setTotalPointsConceded(stats.getTotalPointsConceded() + pointsConceded);

        if (doublesTournament) {
            stats.setDoublesMatchesPlayed(stats.getDoublesMatchesPlayed() + 1);
        } else {
            stats.setSinglesMatchesPlayed(stats.getSinglesMatchesPlayed() + 1);
        }

        if (ObjectUtil.isEqual(match.getRoundName(), RoundNameEnum.LEAGUE_STAGE)) {
            stats.setLeagueMatchesPlayed(stats.getLeagueMatchesPlayed() + 1);
        } else if (AnalyticsUtil.isKnockoutRound(match.getRoundName())) {
            stats.setKnockoutMatchesPlayed(stats.getKnockoutMatchesPlayed() + 1);
        }

        switch (outcome) {
            case WON -> {
                stats.setMatchesWon(stats.getMatchesWon() + 1);
                if (doublesTournament) {
                    stats.setDoublesMatchesWon(stats.getDoublesMatchesWon() + 1);
                } else {
                    stats.setSinglesMatchesWon(stats.getSinglesMatchesWon() + 1);
                }
                if (ObjectUtil.isEqual(match.getRoundName(), RoundNameEnum.LEAGUE_STAGE)) {
                    stats.setLeagueMatchesWon(stats.getLeagueMatchesWon() + 1);
                } else if (AnalyticsUtil.isKnockoutRound(match.getRoundName())) {
                    stats.setKnockoutMatchesWon(stats.getKnockoutMatchesWon() + 1);
                }
                if (AnalyticsUtil.isDominantWin(match.getScoreA(), match.getScoreB())) {
                    stats.setDominantMatchWins(stats.getDominantMatchWins() + 1);
                }
            }
            case LOST -> {
                stats.setMatchesLost(stats.getMatchesLost() + 1);
                if (doublesTournament) {
                    stats.setDoublesMatchesLost(stats.getDoublesMatchesLost() + 1);
                } else {
                    stats.setSinglesMatchesLost(stats.getSinglesMatchesLost() + 1);
                }
            }
            case DRAW -> stats.setMatchesDrawn(stats.getMatchesDrawn() + 1);
        }
    }

    private void applyMatchCountersToTournamentStats(
            PlayerTournamentStats stats,
            MatchOutcome outcome,
            int pointsScored,
            int pointsConceded
    ) {
        stats.setMatchesPlayed(stats.getMatchesPlayed() + 1);
        stats.setPointsScored(stats.getPointsScored() + pointsScored);
        stats.setPointsConceded(stats.getPointsConceded() + pointsConceded);

        switch (outcome) {
            case WON -> stats.setMatchesWon(stats.getMatchesWon() + 1);
            case LOST -> stats.setMatchesLost(stats.getMatchesLost() + 1);
            case DRAW -> stats.setMatchesDrawn(stats.getMatchesDrawn() + 1);
        }
    }

    private void applyMatchCountersToClubStats(
            PlayerClubStats stats,
            MatchOutcome outcome,
            int pointsScored,
            int pointsConceded
    ) {
        stats.setMatchesPlayed(stats.getMatchesPlayed() + 1);
        stats.setTotalPointsScored(stats.getTotalPointsScored() + pointsScored);
        stats.setTotalPointsConceded(stats.getTotalPointsConceded() + pointsConceded);

        switch (outcome) {
            case WON -> stats.setMatchesWon(stats.getMatchesWon() + 1);
            case LOST -> stats.setMatchesLost(stats.getMatchesLost() + 1);
            case DRAW -> stats.setMatchesDrawn(stats.getMatchesDrawn() + 1);
        }
    }

    private void updateStreaks(PlayerLifetimeStats stats, MatchOutcome outcome, LocalDateTime completedAt) {
        if (ObjectUtil.nonNull(stats.getLastMatchAt())
                && completedAt.isBefore(stats.getLastMatchAt())) {
            return;
        }

        stats.setLastMatchAt(completedAt);

        switch (outcome) {
            case WON -> {
                int currentStreak = stats.getCurrentWinStreak() + 1;
                stats.setCurrentWinStreak(currentStreak);
                if (currentStreak > stats.getBestWinStreak()) {
                    stats.setBestWinStreak(currentStreak);
                }
            }
            case LOST -> stats.setCurrentWinStreak(0);
            case DRAW -> {
            }
        }
    }

    private void updateBestRoundReached(PlayerTournamentStats tournamentStats, RoundNameEnum roundName) {
        if (AnalyticsUtil.isHigherRound(roundName, tournamentStats.getBestRoundReached())) {
            tournamentStats.setBestRoundReached(roundName);
        }
    }

    private void updateKnockoutPlacement(
            PlayerTournamentStats tournamentStats,
            TournamentMatch match,
            TournamentMatchPlayer matchPlayer
    ) {
        RoundNameEnum roundName = match.getRoundName();

        if (ObjectUtil.isEqual(roundName, RoundNameEnum.FINAL)) {
            return;
        }

        if (matchPlayer.isWinner()) {
            return;
        }

        if (ObjectUtil.isEqual(roundName, RoundNameEnum.SEMI_FINAL)) {
            tournamentStats.setTournamentPlacement(TournamentPlacementEnum.SEMI_FINALIST);
            return;
        }

        if (ObjectUtil.isEqual(roundName, RoundNameEnum.QUARTER_FINAL)
                && !hasReachedRound(tournamentStats.getBestRoundReached(), RoundNameEnum.SEMI_FINAL)) {
            tournamentStats.setTournamentPlacement(TournamentPlacementEnum.QUARTER_FINALIST);
        }
    }

    private void applyFinalOutcome(List<TournamentMatchPlayer> matchPlayers, TournamentSummary tournamentSummary) {
        for (TournamentMatchPlayer matchPlayer : matchPlayers) {
            Player player = matchPlayer.getPlayer();
            PlayerLifetimeStats lifetimeStats = findOrCreateLifetimeStats(player);
            PlayerTournamentStats tournamentStats = findOrCreateTournamentStats(
                    player,
                    tournamentSummary.getTournament()
            );
            PlayerClubStats clubStats = findOrCreateClubStats(player, tournamentSummary.getTournament().getClub());

            tournamentStats.setBestRoundReached(RoundNameEnum.FINAL);

            if (matchPlayer.isWinner()) {
                tournamentStats.setTournamentPlacement(TournamentPlacementEnum.CHAMPION);
                lifetimeStats.setTournamentWins(lifetimeStats.getTournamentWins() + 1);
                clubStats.setTournamentWins(clubStats.getTournamentWins() + 1);
                tournamentSummary.setChampionPlayer(player);
            } else {
                tournamentStats.setTournamentPlacement(TournamentPlacementEnum.RUNNER_UP);
                lifetimeStats.setTournamentRunnerUp(lifetimeStats.getTournamentRunnerUp() + 1);
                clubStats.setTournamentRunnerUp(clubStats.getTournamentRunnerUp() + 1);
                tournamentSummary.setRunnerUpPlayer(player);
            }

            playerLifetimeStatsRepository.save(lifetimeStats);
            playerTournamentStatsRepository.save(tournamentStats);
            playerClubStatsRepository.save(clubStats);
        }

        tournamentSummary.setStatus(TournamentSummaryStatusEnum.COMPLETED);
    }

    private PartnerSummaryResponse findBestDoublesPartner(Long playerId) {
        List<Object[]> results = tournamentMatchPlayerRepository.findBestDoublesPartner(
                playerId,
                AnalyticsUtil.minPartnerMatches()
        );
        if (results.isEmpty()) {
            return null;
        }

        Object[] row = results.getFirst();
        Long partnerId = ((Number) row[0]).longValue();
        int matchesTogether = ((Number) row[1]).intValue();
        int winsTogether = ((Number) row[2]).intValue();

        Player partner = playerRepository.findById(partnerId)
                .orElseThrow(() -> new PlayerNotFoundException(partnerId));

        return playerAnalyticsMapper.toPartnerSummary(partner, matchesTogether, winsTogether);
    }

    private int resolvePointsScored(TournamentMatch match, PlayerSideEnum side) {
        return ObjectUtil.isEqual(side, PlayerSideEnum.SIDE_A) ? match.getScoreA() : match.getScoreB();
    }

    private int resolvePointsConceded(TournamentMatch match, PlayerSideEnum side) {
        return ObjectUtil.isEqual(side, PlayerSideEnum.SIDE_A) ? match.getScoreB() : match.getScoreA();
    }

    private MatchOutcome resolveOutcome(TournamentMatch match, boolean isWinner) {
        if (ObjectUtil.isEqual(match.getWinnerSide(), WinnerSideEnum.DRAW)) {
            return MatchOutcome.DRAW;
        }
        return isWinner ? MatchOutcome.WON : MatchOutcome.LOST;
    }

    private boolean hasReachedRound(RoundNameEnum currentBest, RoundNameEnum targetRound) {
        if (ObjectUtil.isNull(currentBest)) {
            return false;
        }
        return AnalyticsUtil.roundOrder(currentBest) >= AnalyticsUtil.roundOrder(targetRound);
    }

    private enum MatchOutcome {
        WON,
        LOST,
        DRAW
    }
}
