package com.amigos.courtpulse.analytics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.amigos.courtpulse.dto.analytics.LiveMatchAnalyticsRequest;
import com.amigos.courtpulse.dto.analytics.LiveMatchAnalyticsResponse;
import com.amigos.courtpulse.dto.tournament.SyncMatchResultRequest;
import com.amigos.courtpulse.entity.Club;
import com.amigos.courtpulse.entity.Player;
import com.amigos.courtpulse.entity.TournamentMetadata;
import com.amigos.courtpulse.enums.ClubStatusEnum;
import com.amigos.courtpulse.enums.PlayerSideEnum;
import com.amigos.courtpulse.enums.PlayerStatusEnum;
import com.amigos.courtpulse.enums.RoundNameEnum;
import com.amigos.courtpulse.enums.TournamentStatusEnum;
import com.amigos.courtpulse.enums.TournamentTypeEnum;
import com.amigos.courtpulse.enums.WinnerSideEnum;
import com.amigos.courtpulse.exception.PlayerNotFoundException;
import com.amigos.courtpulse.repository.ClubRepository;
import com.amigos.courtpulse.repository.PlayerRepository;
import com.amigos.courtpulse.repository.TournamentMetadataRepository;
import com.amigos.courtpulse.service.LiveMatchAnalyticsService;
import com.amigos.courtpulse.service.TournamentMatchService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class LiveMatchAnalyticsIntegrationTest {

    @Autowired
    private LiveMatchAnalyticsService liveMatchAnalyticsService;

    @Autowired
    private TournamentMatchService tournamentMatchService;

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
    void singlesLiveMatchAnalyticsReturnsZerosBeforeAnyMatch() {
        LiveMatchAnalyticsResponse response = liveMatchAnalyticsService.getLiveMatchAnalytics(
                singlesTournament.getId(),
                new LiveMatchAnalyticsRequest(
                        List.of(playerA.getPlayerCode()),
                        List.of(playerB.getPlayerCode())
                )
        );

        assertEquals(singlesTournament.getId(), response.tournamentId());
        assertEquals(TournamentTypeEnum.SINGLES, response.tournamentType());
        assertEquals(club.getId(), response.clubId());
        assertEquals(club.getClubCode(), response.clubCode());
        assertEquals(1, response.teamA().players().size());
        assertEquals(0, response.teamA().players().getFirst().matchesPlayed());
        assertNull(response.teamA().partnership());
        assertEquals(0, response.headToHead().matchesPlayed());
    }

    @Test
    void singlesLiveMatchAnalyticsReturnsTournamentStatsAndHeadToHead() {
        tournamentMatchService.syncMatchResult(
                singlesTournament.getId(),
                singlesMatch(RoundNameEnum.LEAGUE_STAGE, 1, 21, 15, WinnerSideEnum.SIDE_A, playerA, true, playerB, false)
        );

        LiveMatchAnalyticsResponse response = liveMatchAnalyticsService.getLiveMatchAnalytics(
                singlesTournament.getId(),
                new LiveMatchAnalyticsRequest(
                        List.of(playerA.getPlayerCode()),
                        List.of(playerB.getPlayerCode())
                )
        );

        assertEquals(1, response.teamA().players().getFirst().matchesPlayed());
        assertEquals(1, response.teamA().players().getFirst().matchesWon());
        assertEquals(21, response.teamA().players().getFirst().pointsScored());
        assertEquals(1, response.teamB().players().getFirst().matchesLost());
        assertEquals(1, response.headToHead().matchesPlayed());
        assertEquals(1, response.headToHead().teamAWins());
        assertEquals(0, response.headToHead().teamBWins());
    }

    @Test
    void doublesLiveMatchAnalyticsReturnsPartnershipAndHeadToHead() {
        tournamentMatchService.syncMatchResult(
                doublesTournament.getId(),
                doublesMatch(RoundNameEnum.LEAGUE_STAGE, 1, 21, 18, WinnerSideEnum.SIDE_A, playerA, playerB, playerC, playerD)
        );

        LiveMatchAnalyticsResponse response = liveMatchAnalyticsService.getLiveMatchAnalytics(
                doublesTournament.getId(),
                new LiveMatchAnalyticsRequest(
                        List.of(playerA.getPlayerCode(), playerB.getPlayerCode()),
                        List.of(playerC.getPlayerCode(), playerD.getPlayerCode())
                )
        );

        assertEquals(TournamentTypeEnum.FIXED_DOUBLES, response.tournamentType());
        assertEquals(1, response.teamA().players().getFirst().matchesPlayed());
        assertEquals(1, response.teamA().partnership().matchesTogether());
        assertEquals(1, response.teamA().partnership().winsTogether());
        assertEquals(1, response.teamB().partnership().matchesTogether());
        assertEquals(0, response.teamB().partnership().winsTogether());
        assertEquals(1, response.headToHead().matchesPlayed());
        assertEquals(1, response.headToHead().teamAWins());
    }

    @Test
    void doublesHeadToHeadCountsMultipleMeetings() {
        tournamentMatchService.syncMatchResult(
                doublesTournament.getId(),
                doublesMatch(RoundNameEnum.LEAGUE_STAGE, 1, 21, 18, WinnerSideEnum.SIDE_A, playerA, playerB, playerC, playerD)
        );
        tournamentMatchService.syncMatchResult(
                doublesTournament.getId(),
                doublesMatch(RoundNameEnum.LEAGUE_STAGE, 2, 19, 21, WinnerSideEnum.SIDE_B, playerA, playerB, playerC, playerD)
        );

        LiveMatchAnalyticsResponse response = liveMatchAnalyticsService.getLiveMatchAnalytics(
                doublesTournament.getId(),
                new LiveMatchAnalyticsRequest(
                        List.of(playerA.getPlayerCode(), playerB.getPlayerCode()),
                        List.of(playerC.getPlayerCode(), playerD.getPlayerCode())
                )
        );

        assertEquals(2, response.headToHead().matchesPlayed());
        assertEquals(1, response.headToHead().teamAWins());
        assertEquals(1, response.headToHead().teamBWins());
        assertEquals(2, response.teamA().partnership().matchesTogether());
        assertEquals(1, response.teamA().partnership().winsTogether());
    }

    @Test
    void rejectsTeamSizeMismatchForSinglesTournament() {
        assertThrows(IllegalArgumentException.class, () ->
                liveMatchAnalyticsService.getLiveMatchAnalytics(
                        singlesTournament.getId(),
                        new LiveMatchAnalyticsRequest(
                                List.of(playerA.getPlayerCode(), playerB.getPlayerCode()),
                                List.of(playerC.getPlayerCode())
                        )
                ));
    }

    @Test
    void rejectsUnknownPlayerCode() {
        assertThrows(PlayerNotFoundException.class, () ->
                liveMatchAnalyticsService.getLiveMatchAnalytics(
                        singlesTournament.getId(),
                        new LiveMatchAnalyticsRequest(
                                List.of(playerA.getPlayerCode()),
                                List.of("CPZZZZ")
                        )
                ));
    }

    @Test
    void rejectsDuplicatePlayerCodesAcrossTeams() {
        assertThrows(IllegalArgumentException.class, () ->
                liveMatchAnalyticsService.getLiveMatchAnalytics(
                        singlesTournament.getId(),
                        new LiveMatchAnalyticsRequest(
                                List.of(playerA.getPlayerCode()),
                                List.of(playerA.getPlayerCode())
                        )
                ));
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
