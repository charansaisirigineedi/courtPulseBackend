package com.amigos.courtpulse.analytics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

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
import com.amigos.courtpulse.repository.ClubRepository;
import com.amigos.courtpulse.repository.PlayerRepository;
import com.amigos.courtpulse.repository.TournamentMatchPlayerRepository;
import com.amigos.courtpulse.repository.TournamentMatchRepository;
import com.amigos.courtpulse.repository.TournamentMetadataRepository;
import com.amigos.courtpulse.repository.PlayerLifetimeStatsRepository;
import com.amigos.courtpulse.service.TournamentMatchService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class PlayerAnalyticsRollbackTest {

    @Autowired
    private TournamentMatchService tournamentMatchService;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private TournamentMetadataRepository tournamentMetadataRepository;

    @Autowired
    private TournamentMatchRepository tournamentMatchRepository;

    @Autowired
    private TournamentMatchPlayerRepository tournamentMatchPlayerRepository;

    @MockitoBean
    private PlayerLifetimeStatsRepository playerLifetimeStatsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanMatchData() {
        tournamentMatchPlayerRepository.deleteAll();
        tournamentMatchRepository.deleteAll();
    }

    @Test
    void analyticsFailureRollsBackMatchPersistence() {
        when(playerLifetimeStatsRepository.findByPlayerIdForUpdate(any())).thenReturn(Optional.empty());
        doThrow(new RuntimeException("analytics failed"))
                .when(playerLifetimeStatsRepository)
                .save(any());

        Player playerA = playerRepository.save(Player.builder()
                .playerCode("CPROLLA")
                .username("rolla")
                .passwordHash(passwordEncoder.encode("password"))
                .name("Roll A")
                .status(PlayerStatusEnum.ACTIVE)
                .build());
        Player playerB = playerRepository.save(Player.builder()
                .playerCode("CPROLLB")
                .username("rollb")
                .passwordHash(passwordEncoder.encode("password"))
                .name("Roll B")
                .status(PlayerStatusEnum.ACTIVE)
                .build());
        Club club = clubRepository.save(Club.builder()
                .clubCode("CLROLL001")
                .clubName("Rollback Club")
                .owner(playerA)
                .status(ClubStatusEnum.ACTIVE)
                .build());
        TournamentMetadata tournament = tournamentMetadataRepository.save(TournamentMetadata.builder()
                .tournamentCode("TROLL001")
                .club(club)
                .tournamentName("Rollback Tournament")
                .tournamentType(TournamentTypeEnum.SINGLES)
                .status(TournamentStatusEnum.DRAFT)
                .tournamentDate(LocalDate.now())
                .createdBy(playerA)
                .build());

        SyncMatchResultRequest request = new SyncMatchResultRequest(
                RoundNameEnum.LEAGUE_STAGE,
                1,
                21,
                10,
                WinnerSideEnum.SIDE_A,
                LocalDateTime.now(),
                List.of(
                        new SyncMatchResultRequest.MatchPlayerRequest(playerA.getId(), PlayerSideEnum.SIDE_A, true),
                        new SyncMatchResultRequest.MatchPlayerRequest(playerB.getId(), PlayerSideEnum.SIDE_B, false)
                )
        );

        assertThrows(RuntimeException.class, () ->
                tournamentMatchService.syncMatchResult(tournament.getId(), request));

        assertEquals(0, tournamentMatchRepository.count());
    }
}
