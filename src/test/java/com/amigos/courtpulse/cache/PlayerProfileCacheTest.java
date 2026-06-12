package com.amigos.courtpulse.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amigos.courtpulse.dto.player.PlayerProfileResponse;
import com.amigos.courtpulse.entity.Player;
import com.amigos.courtpulse.enums.PlayerStatusEnum;
import com.amigos.courtpulse.mapper.PlayerMapper;
import com.amigos.courtpulse.repository.PlayerRepository;
import com.amigos.courtpulse.service.CacheEvictionService;
import com.amigos.courtpulse.service.impl.PlayerServiceImpl;
import com.amigos.courtpulse.util.CacheNames;
import com.amigos.courtpulse.util.PlayerCodeGenerator;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class PlayerProfileCacheTest {

    @Autowired
    private PlayerServiceImpl playerService;

    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
    private PlayerRepository playerRepository;

    @MockitoBean
    private PlayerMapper playerMapper;

    @MockitoBean
    private PlayerCodeGenerator playerCodeGenerator;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private CacheEvictionService cacheEvictionService;

    @Test
    void getPlayerByCodeUsesCacheOnSecondCall() {
        Player player = Player.builder()
                .playerCode("CP0001")
                .username("alpha")
                .name("Alpha")
                .status(PlayerStatusEnum.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
        PlayerProfileResponse response = new PlayerProfileResponse(
                "CP0001",
                "alpha",
                "Alpha",
                null,
                null,
                false,
                PlayerStatusEnum.ACTIVE,
                player.getCreatedAt()
        );

        when(playerRepository.findByPlayerCode("CP0001")).thenReturn(Optional.of(player));
        when(playerMapper.toProfileResponse(player)).thenReturn(response);

        PlayerProfileResponse first = playerService.getPlayerByCode("cp0001");
        PlayerProfileResponse second = playerService.getPlayerByCode("CP0001");

        assertThat(first).isEqualTo(second);
        verify(playerRepository, times(1)).findByPlayerCode("CP0001");
        assertThat(cacheManager.getCache(CacheNames.PLAYER_PROFILES).get("CP0001", PlayerProfileResponse.class))
                .isEqualTo(response);
    }
}
