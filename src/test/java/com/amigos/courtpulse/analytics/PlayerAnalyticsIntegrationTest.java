package com.amigos.courtpulse.analytics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.amigos.courtpulse.dto.analytics.PlayerLifetimeAnalyticsResponse;
import com.amigos.courtpulse.dto.analytics.TournamentSummaryResponse;
import com.amigos.courtpulse.dto.tournament.SyncMatchResultRequest;
import com.amigos.courtpulse.entity.Club;
import com.amigos.courtpulse.entity.Player;
import com.amigos.courtpulse.entity.TournamentMatch;
import com.amigos.courtpulse.entity.TournamentMetadata;
import com.amigos.courtpulse.enums.ClubStatusEnum;
import com.amigos.courtpulse.enums.PlayerSideEnum;
import com.amigos.courtpulse.enums.PlayerStatusEnum;
import com.amigos.courtpulse.enums.RoundNameEnum;
import com.amigos.courtpulse.enums.TournamentPlacementEnum;
import com.amigos.courtpulse.enums.TournamentStatusEnum;
import com.amigos.courtpulse.enums.TournamentSummaryStatusEnum;
import com.amigos.courtpulse.enums.TournamentTypeEnum;
import com.amigos.courtpulse.enums.WinnerSideEnum;
import com.amigos.courtpulse.repository.ClubRepository;
import com.amigos.courtpulse.repository.PlayerRepository;
import com.amigos.courtpulse.repository.TournamentMatchRepository;
import com.amigos.courtpulse.repository.TournamentMetadataRepository;
import com.amigos.courtpulse.service.PlayerAnalyticsService;
import com.amigos.courtpulse.service.TournamentMatchService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PlayerAnalyticsIntegrationTest {

    @Autowired
    private TournamentMatchService tournamentMatchService;

    @Autowired
    private PlayerAnalyticsService playerAnalyticsService;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private TournamentMetadataRepository tournamentMetadataRepository;

    @Autowired
    private TournamentMatchRepository tournamentMatchRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Player playerA;
    private Player playerB;
    private Player playerC;
    private Player playerD;
    private Club club;
    private TournamentMetadata singlesTournament;
    private TournamentMetadata doublesTournament;
    private String suffix;

    @BeforeEach
    void setUp() {
        suffix = Long.toString(System.nanoTime(), 36);

        playerA = savePlayer("alpha" + suffix, "Alpha Player", "CPA" + suffix);
        playerB = savePlayer("beta" + suffix, "Beta Player", "CPB" + suffix);
        playerC = savePlayer("gamma" + suffix, "Gamma Player", "CPC" + suffix);
        playerD = savePlayer("delta" + suffix, "Delta Player", "CPD" + suffix);

        club = clubRepository.save(Club.builder()
                .clubCode("CLB" + suffix.substring(Math.max(0, suffix.length() - 7)))
                .clubName("Analytics Club")
                .owner(playerA)
                .status(ClubStatusEnum.ACTIVE)
                .build());

        singlesTournament = saveTournament("Singles" + suffix, TournamentTypeEnum.SINGLES, "S");
        doublesTournament = saveTournament("Doubles" + suffix, TournamentTypeEnum.FIXED_DOUBLES, "D");
    }

    @Test
    void singlesMatchUpdatesLifetimeStatsAndMarksMatchProcessed() {
        SyncMatchResultRequest request = singlesMatch(
                RoundNameEnum.LEAGUE_STAGE,
                1,
                21,
                15,
                WinnerSideEnum.SIDE_A,
                playerA,
                true,
                playerB,
                false
        );

        tournamentMatchService.syncMatchResult(singlesTournament.getId(), request);

        TournamentMatch savedMatch = tournamentMatchRepository.findAll().getFirst();
        assertNotNull(savedMatch.getAnalyticsProcessedAt());

        PlayerLifetimeAnalyticsResponse stats = playerAnalyticsService.getLifetimeAnalytics(playerA.getPlayerCode());
        assertEquals(1, stats.matchesPlayed());
        assertEquals(1, stats.matchesWon());
        assertEquals(0, stats.matchesLost());
        assertEquals(21, stats.totalPointsScored());
        assertEquals(15, stats.totalPointsConceded());
        assertEquals(1, stats.singlesMatchesPlayed());
        assertEquals(1, stats.singlesMatchesWon());
    }

    @Test
    void doublesMatchUpdatesDoublesCounters() {
        SyncMatchResultRequest request = doublesMatch(
                RoundNameEnum.LEAGUE_STAGE,
                1,
                21,
                18,
                WinnerSideEnum.SIDE_A,
                playerA,
                playerB,
                playerC,
                playerD
        );

        tournamentMatchService.syncMatchResult(doublesTournament.getId(), request);

        PlayerLifetimeAnalyticsResponse statsA = playerAnalyticsService.getLifetimeAnalytics(playerA.getPlayerCode());
        PlayerLifetimeAnalyticsResponse statsC = playerAnalyticsService.getLifetimeAnalytics(playerC.getPlayerCode());

        assertEquals(1, statsA.doublesMatchesPlayed());
        assertEquals(1, statsA.doublesMatchesWon());
        assertEquals(21, statsA.totalPointsScored());
        assertEquals(1, statsC.doublesMatchesPlayed());
        assertEquals(1, statsC.doublesMatchesLost());
        assertEquals(18, statsC.totalPointsScored());
    }

    @Test
    void finalMatchSetsChampionRunnerUpAndTournamentSummary() {
        tournamentMatchService.syncMatchResult(
                singlesTournament.getId(),
                singlesMatch(RoundNameEnum.SEMI_FINAL, 1, 21, 10, WinnerSideEnum.SIDE_A, playerA, true, playerB, false)
        );
        tournamentMatchService.syncMatchResult(
                singlesTournament.getId(),
                singlesMatch(RoundNameEnum.SEMI_FINAL, 2, 21, 12, WinnerSideEnum.SIDE_A, playerC, true, playerD, false)
        );
        tournamentMatchService.syncMatchResult(
                singlesTournament.getId(),
                singlesMatch(RoundNameEnum.FINAL, 1, 21, 19, WinnerSideEnum.SIDE_A, playerA, true, playerC, false)
        );

        PlayerLifetimeAnalyticsResponse championStats =
                playerAnalyticsService.getLifetimeAnalytics(playerA.getPlayerCode());
        PlayerLifetimeAnalyticsResponse runnerUpStats =
                playerAnalyticsService.getLifetimeAnalytics(playerC.getPlayerCode());

        assertEquals(1, championStats.tournamentWins());
        assertEquals(1, runnerUpStats.tournamentRunnerUp());

        TournamentSummaryResponse summary =
                playerAnalyticsService.getTournamentSummary(singlesTournament.getId());
        assertEquals(TournamentSummaryStatusEnum.COMPLETED, summary.status());
        assertEquals(playerA.getPlayerCode(), summary.championPlayerCode());
        assertEquals(playerC.getPlayerCode(), summary.runnerUpPlayerCode());
        assertEquals(3, summary.totalMatchesPlayed());
        assertEquals(4, summary.participantCount());

        assertEquals(
                TournamentPlacementEnum.CHAMPION,
                playerAnalyticsService.getTournamentAnalytics(playerA.getPlayerCode(), null, PageRequest.of(0, 20))
                        .getFirst().tournamentPlacement()
        );
        assertEquals(
                TournamentPlacementEnum.RUNNER_UP,
                playerAnalyticsService.getTournamentAnalytics(playerC.getPlayerCode(), null, PageRequest.of(0, 20))
                        .getFirst().tournamentPlacement()
        );
    }

    @Test
    void finalMatchSetsTournamentWinsAndRunnerUp() {
        tournamentMatchService.syncMatchResult(
                singlesTournament.getId(),
                singlesMatch(RoundNameEnum.SEMI_FINAL, 1, 21, 17, WinnerSideEnum.SIDE_A, playerA, true, playerB, false)
        );
        tournamentMatchService.syncMatchResult(
                singlesTournament.getId(),
                singlesMatch(RoundNameEnum.FINAL, 1, 21, 14, WinnerSideEnum.SIDE_A, playerA, true, playerB, false)
        );

        PlayerLifetimeAnalyticsResponse winnerStats =
                playerAnalyticsService.getLifetimeAnalytics(playerA.getPlayerCode());
        PlayerLifetimeAnalyticsResponse loserStats =
                playerAnalyticsService.getLifetimeAnalytics(playerB.getPlayerCode());

        assertEquals(1, winnerStats.tournamentWins());
        assertEquals(0, winnerStats.tournamentRunnerUp());
        assertEquals(0, loserStats.tournamentWins());
        assertEquals(1, loserStats.tournamentRunnerUp());
    }

    @Test
    void drawMatchIncrementsDrawCounterWithoutWinLoss() {
        tournamentMatchService.syncMatchResult(
                singlesTournament.getId(),
                new SyncMatchResultRequest(
                        RoundNameEnum.LEAGUE_STAGE,
                        1,
                        20,
                        20,
                        WinnerSideEnum.DRAW,
                        LocalDateTime.now(),
                        List.of(
                                new SyncMatchResultRequest.MatchPlayerRequest(playerA.getId(), PlayerSideEnum.SIDE_A, false),
                                new SyncMatchResultRequest.MatchPlayerRequest(playerB.getId(), PlayerSideEnum.SIDE_B, false)
                        )
                )
        );

        PlayerLifetimeAnalyticsResponse statsA = playerAnalyticsService.getLifetimeAnalytics(playerA.getPlayerCode());
        assertEquals(1, statsA.matchesPlayed());
        assertEquals(1, statsA.matchesDrawn());
        assertEquals(0, statsA.matchesWon());
        assertEquals(0, statsA.matchesLost());
        assertNull(statsA.winRatio());
    }

    @Test
    void duplicateMatchSyncRollsBackAndDoesNotDoubleStats() {
        SyncMatchResultRequest request = singlesMatch(
                RoundNameEnum.LEAGUE_STAGE,
                1,
                21,
                12,
                WinnerSideEnum.SIDE_A,
                playerA,
                true,
                playerB,
                false
        );

        tournamentMatchService.syncMatchResult(singlesTournament.getId(), request);
        assertThrows(DataIntegrityViolationException.class, () ->
                tournamentMatchService.syncMatchResult(singlesTournament.getId(), request));

        assertEquals(1, tournamentMatchRepository.count());
        PlayerLifetimeAnalyticsResponse stats = playerAnalyticsService.getLifetimeAnalytics(playerA.getPlayerCode());
        assertEquals(1, stats.matchesPlayed());
        assertEquals(1, stats.matchesWon());
    }

    @Test
    void processMatchSkipsWhenAlreadyProcessed() {
        SyncMatchResultRequest request = singlesMatch(
                RoundNameEnum.LEAGUE_STAGE,
                2,
                21,
                9,
                WinnerSideEnum.SIDE_A,
                playerA,
                true,
                playerB,
                false
        );

        var response = tournamentMatchService.syncMatchResult(singlesTournament.getId(), request);
        playerAnalyticsService.processMatch(response.id());

        PlayerLifetimeAnalyticsResponse stats = playerAnalyticsService.getLifetimeAnalytics(playerA.getPlayerCode());
        assertEquals(1, stats.matchesPlayed());
        assertEquals(1, stats.matchesWon());
    }

    private Player savePlayer(String username, String name, String playerCode) {
        return playerRepository.save(Player.builder()
                .playerCode(playerCode.length() > 10 ? playerCode.substring(0, 10) : playerCode)
                .username(username)
                .passwordHash(passwordEncoder.encode("password"))
                .name(name)
                .status(PlayerStatusEnum.ACTIVE)
                .build());
    }

    private TournamentMetadata saveTournament(String name, TournamentTypeEnum type, String typePrefix) {
        String code = ("T" + typePrefix + suffix).substring(0, Math.min(10, ("T" + typePrefix + suffix).length()));
        return tournamentMetadataRepository.save(TournamentMetadata.builder()
                .tournamentCode(code.toUpperCase())
                .club(club)
                .tournamentName(name)
                .tournamentType(type)
                .status(TournamentStatusEnum.DRAFT)
                .tournamentDate(LocalDate.now())
                .createdBy(playerA)
                .build());
    }

    private SyncMatchResultRequest singlesMatch(
            RoundNameEnum round,
            int matchNo,
            int scoreA,
            int scoreB,
            WinnerSideEnum winnerSide,
            Player sideAPlayer,
            boolean sideAWins,
            Player sideBPlayer,
            boolean sideBWins
    ) {
        return new SyncMatchResultRequest(
                round,
                matchNo,
                scoreA,
                scoreB,
                winnerSide,
                LocalDateTime.now(),
                List.of(
                        new SyncMatchResultRequest.MatchPlayerRequest(sideAPlayer.getId(), PlayerSideEnum.SIDE_A, sideAWins),
                        new SyncMatchResultRequest.MatchPlayerRequest(sideBPlayer.getId(), PlayerSideEnum.SIDE_B, sideBWins)
                )
        );
    }

    private SyncMatchResultRequest doublesMatch(
            RoundNameEnum round,
            int matchNo,
            int scoreA,
            int scoreB,
            WinnerSideEnum winnerSide,
            Player sideAPlayerOne,
            Player sideAPlayerTwo,
            Player sideBPlayerOne,
            Player sideBPlayerTwo
    ) {
        boolean sideAWins = WinnerSideEnum.SIDE_A == winnerSide;
        return new SyncMatchResultRequest(
                round,
                matchNo,
                scoreA,
                scoreB,
                winnerSide,
                LocalDateTime.now(),
                List.of(
                        new SyncMatchResultRequest.MatchPlayerRequest(
                                sideAPlayerOne.getId(), PlayerSideEnum.SIDE_A, sideAWins),
                        new SyncMatchResultRequest.MatchPlayerRequest(
                                sideAPlayerTwo.getId(), PlayerSideEnum.SIDE_A, sideAWins),
                        new SyncMatchResultRequest.MatchPlayerRequest(
                                sideBPlayerOne.getId(), PlayerSideEnum.SIDE_B, !sideAWins),
                        new SyncMatchResultRequest.MatchPlayerRequest(
                                sideBPlayerTwo.getId(), PlayerSideEnum.SIDE_B, !sideAWins)
                )
        );
    }
}
