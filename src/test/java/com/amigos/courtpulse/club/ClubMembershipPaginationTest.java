package com.amigos.courtpulse.club;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amigos.courtpulse.dto.club.MyClubResponse;
import com.amigos.courtpulse.entity.Club;
import com.amigos.courtpulse.entity.ClubMember;
import com.amigos.courtpulse.entity.Player;
import com.amigos.courtpulse.enums.ClubMemberRoleEnum;
import com.amigos.courtpulse.enums.ClubStatusEnum;
import com.amigos.courtpulse.enums.PlayerStatusEnum;
import com.amigos.courtpulse.repository.ClubMemberRepository;
import com.amigos.courtpulse.repository.ClubRepository;
import com.amigos.courtpulse.repository.PlayerRepository;
import com.amigos.courtpulse.service.ClubService;
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
class ClubMembershipPaginationTest {

    private static final int MAX_MY_CLUBS_LIMIT = 50;

    @Autowired
    private ClubService clubService;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private ClubMemberRepository clubMemberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Player player;
    private String suffix;

    @BeforeEach
    void setUp() {
        suffix = Long.toString(System.nanoTime(), 36);
        String playerCode = ("CP" + suffix).substring(0, Math.min(10, ("CP" + suffix).length()))
                .toUpperCase(java.util.Locale.ROOT);
        player = playerRepository.save(Player.builder()
                .playerCode(playerCode)
                .username("member" + suffix)
                .passwordHash(passwordEncoder.encode("password"))
                .name("Member Player")
                .status(PlayerStatusEnum.ACTIVE)
                .build());
    }

    @Test
    void getMyClubsUsesDefaultLimitAndSupportsOffset() {
        createMemberships(25);

        List<MyClubResponse> firstPage = clubService.getMyClubs(player.getPlayerCode(), PageRequest.of(0, 20));
        List<MyClubResponse> secondPage = clubService.getMyClubs(player.getPlayerCode(), PageRequest.of(1, 20));

        assertEquals(20, firstPage.size());
        assertEquals(5, secondPage.size());
        assertTrue(firstPage.stream().noneMatch(first ->
                secondPage.stream().anyMatch(second -> second.clubCode().equals(first.clubCode()))));
    }

    @Test
    void getMyClubsClampsLimitToFifty() {
        createMemberships(55);

        List<MyClubResponse> clubs = clubService.getMyClubs(player.getPlayerCode(), PageRequest.of(0, 100));

        assertEquals(MAX_MY_CLUBS_LIMIT, clubs.size());
    }

    private void createMemberships(int count) {
        LocalDateTime joinedAt = LocalDateTime.now();
        for (int i = 0; i < count; i++) {
            String clubCode = String.format("C%s%03d", suffix.substring(0, Math.min(5, suffix.length())), i)
                    .toUpperCase(java.util.Locale.ROOT);
            Club club = clubRepository.save(Club.builder()
                    .clubCode(clubCode)
                    .clubName("Club " + i)
                    .owner(player)
                    .status(ClubStatusEnum.ACTIVE)
                    .build());

            clubMemberRepository.save(ClubMember.builder()
                    .club(club)
                    .player(player)
                    .role(ClubMemberRoleEnum.MEMBER)
                    .joinedAt(joinedAt.minusDays(count - i))
                    .build());
        }
    }
}
