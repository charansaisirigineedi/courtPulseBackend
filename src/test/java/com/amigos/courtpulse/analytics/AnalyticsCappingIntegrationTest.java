package com.amigos.courtpulse.analytics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.amigos.courtpulse.dto.analytics.PlayerClubAnalyticsResponse;
import com.amigos.courtpulse.dto.analytics.PlayerLifetimeAnalyticsResponse;
import com.amigos.courtpulse.dto.analytics.PlayerTournamentAnalyticsResponse;
import com.amigos.courtpulse.dto.tournament.SyncMatchResultRequest;
import com.amigos.courtpulse.entity.Club;
import com.amigos.courtpulse.entity.Player;
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
import com.amigos.courtpulse.repository.TournamentMetadataRepository;
import com.amigos.courtpulse.service.PlayerAnalyticsService;
import com.amigos.courtpulse.service.TournamentMatchService;
import com.amigos.courtpulse.util.AnalyticsUtil;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AnalyticsCappingIntegrationTest {

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
    private PasswordEncoder passwordEncoder;

    private Player playerA;
    private Player playerB;
    private Player playerC;
    private Player playerD;
    private Club club;
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
    }

    @Test
    void lifetimeAnalyticsCapsRecentTournamentWinsAtFive() {
        for (int i = 0; i < 6; i++) {
            TournamentMetadata tournament = saveTournament("Win Tournament " + i, "W" + i);
            completeTournamentWithChampion(tournament, playerA, playerB, playerC, playerD);
        }

        PlayerLifetimeAnalyticsResponse stats = playerAnalyticsService.getLifetimeAnalytics(playerA.getPlayerCode());

        assertEquals(6, stats.tournamentWins());
        assertEquals(AnalyticsUtil.recentTournamentWinsPreviewLimit(), stats.recentTournamentWins().size());
    }

    @Test
    void tournamentAnalyticsRespectsPlacementFilterAndLimit() {
        TournamentMetadata wonTournament = saveTournament("Won Tournament", "W1");
        completeTournamentWithChampion(wonTournament, playerA, playerB, playerC, playerD);

        TournamentMetadata lostTournament = saveTournament("Lost Tournament", "L1");
        completeTournamentWithChampion(lostTournament, playerB, playerA, playerC, playerD);

        List<PlayerTournamentAnalyticsResponse> championOnly =
                playerAnalyticsService.getTournamentAnalytics(
                        playerA.getPlayerCode(),
                        TournamentPlacementEnum.CHAMPION,
                        PageRequest.of(0, 1)
                );

        assertEquals(1, championOnly.size());
        assertEquals(TournamentPlacementEnum.CHAMPION, championOnly.getFirst().tournamentPlacement());

        List<PlayerTournamentAnalyticsResponse> limited =
                playerAnalyticsService.getTournamentAnalytics(
                        playerA.getPlayerCode(),
                        null,
                        PageRequest.of(0, 1)
                );

        assertEquals(1, limited.size());
    }

    @Test
    void clubAnalyticsCapsRecentCompletedTournamentsAtFive() {
        for (int i = 0; i < 6; i++) {
            TournamentMetadata tournament = saveTournament("Club Tournament " + i, "C" + i);
            completeTournamentWithChampion(tournament, playerA, playerB, playerC, playerD);
        }

        tournamentMatchService.syncMatchResult(
                saveTournament("Participation Only", "P1").getId(),
                singlesMatch(RoundNameEnum.LEAGUE_STAGE, 1, 21, 10, WinnerSideEnum.SIDE_A, playerA, true, playerB, false)
        );

        PlayerClubAnalyticsResponse clubStats =
                playerAnalyticsService.getClubAnalytics(playerA.getPlayerCode(), club.getClubCode());

        assertEquals(
                AnalyticsUtil.recentClubCompletedTournamentsLimit(),
                clubStats.recentCompletedTournaments().size()
        );
        assertEquals(TournamentSummaryStatusEnum.COMPLETED, playerAnalyticsService
                .getTournamentSummary(clubStats.recentCompletedTournaments().getFirst().tournamentId())
                .status());
    }

    @Test
    void playerTournamentAnalyticsByIdReturnsStatsOrFailsWhenNotParticipated() {
        TournamentMetadata tournament = saveTournament("Drill Down", "D1");
        completeTournamentWithChampion(tournament, playerA, playerB, playerC, playerD);

        PlayerTournamentAnalyticsResponse stats =
                playerAnalyticsService.getPlayerTournamentAnalytics(playerA.getPlayerCode(), tournament.getId());

        assertEquals(TournamentPlacementEnum.CHAMPION, stats.tournamentPlacement());
        assertEquals(tournament.getId(), stats.tournamentId());

        assertThrows(
                IllegalArgumentException.class,
                () -> playerAnalyticsService.getPlayerTournamentAnalytics(
                        savePlayer("epsilon" + suffix, "Epsilon Player", "CPE" + suffix).getPlayerCode(),
                        tournament.getId()
                )
        );
    }

    private void completeTournamentWithChampion(
            TournamentMetadata tournament,
            Player champion,
            Player runnerUpCandidate,
            Player semiOpponentOne,
            Player semiOpponentTwo
    ) {
        tournamentMatchService.syncMatchResult(
                tournament.getId(),
                singlesMatch(RoundNameEnum.SEMI_FINAL, 1, 21, 10, WinnerSideEnum.SIDE_A, champion, true, semiOpponentOne, false)
        );
        tournamentMatchService.syncMatchResult(
                tournament.getId(),
                singlesMatch(RoundNameEnum.SEMI_FINAL, 2, 21, 12, WinnerSideEnum.SIDE_A, runnerUpCandidate, true, semiOpponentTwo, false)
        );
        tournamentMatchService.syncMatchResult(
                tournament.getId(),
                singlesMatch(RoundNameEnum.FINAL, 1, 21, 19, WinnerSideEnum.SIDE_A, champion, true, runnerUpCandidate, false)
        );
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

    private TournamentMetadata saveTournament(String name, String typePrefix) {
        String code = ("T" + typePrefix + suffix).substring(0, Math.min(10, ("T" + typePrefix + suffix).length()));
        return tournamentMetadataRepository.save(TournamentMetadata.builder()
                .tournamentCode(code.toUpperCase())
                .club(club)
                .tournamentName(name)
                .tournamentType(TournamentTypeEnum.SINGLES)
                .status(TournamentStatusEnum.DRAFT)
                .tournamentDate(LocalDate.now().minusDays(code.length()))
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
}
