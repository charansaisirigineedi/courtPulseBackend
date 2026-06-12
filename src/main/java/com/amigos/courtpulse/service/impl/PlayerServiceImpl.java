package com.amigos.courtpulse.service.impl;

import com.amigos.courtpulse.dto.player.CreatePlayerRequest;
import com.amigos.courtpulse.dto.player.PlayerProfileResponse;
import com.amigos.courtpulse.dto.player.PlayerSearchResponse;
import com.amigos.courtpulse.dto.player.UpdatePlayerRequest;
import com.amigos.courtpulse.entity.Player;
import com.amigos.courtpulse.enums.PlayerStatusEnum;
import com.amigos.courtpulse.exception.EmailAlreadyExistsException;
import com.amigos.courtpulse.exception.PlayerNotFoundException;
import com.amigos.courtpulse.exception.UsernameAlreadyExistsException;
import com.amigos.courtpulse.mapper.PlayerMapper;
import com.amigos.courtpulse.repository.PlayerRepository;
import com.amigos.courtpulse.service.PlayerService;
import com.amigos.courtpulse.util.ObjectUtil;
import com.amigos.courtpulse.util.PlayerCodeGenerator;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class PlayerServiceImpl implements PlayerService {

    private static final Logger log = LoggerFactory.getLogger(PlayerServiceImpl.class);
    private static final int SEARCH_LIMIT = 20;

    private final PlayerRepository playerRepository;
    private final PlayerMapper playerMapper;
    private final PlayerCodeGenerator playerCodeGenerator;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public PlayerProfileResponse createPlayer(CreatePlayerRequest request) {
        String username = normalizeRequired(request.username());
        String email = normalizeEmail(request.email());

        if (playerRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException(username);
        }
        if (ObjectUtil.nonNull(email) && playerRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        Player player = Player.builder()
                .playerCode(playerCodeGenerator.generateUniqueCode())
                .username(username)
                .passwordHash(passwordEncoder.encode(request.password()))
                .name(normalizeRequired(request.name()))
                .gameName(normalizeOptional(request.gameName()))
                .email(email)
                .emailVerified(false)
                .status(PlayerStatusEnum.ACTIVE)
                .build();

        Player savedPlayer = playerRepository.save(player);
        log.info("Created player with code {}", savedPlayer.getPlayerCode());
        return playerMapper.toProfileResponse(savedPlayer);
    }

    @Override
    @Transactional(readOnly = true)
    public PlayerProfileResponse getPlayerByCode(String playerCode) {
        Player player = findPlayerByCode(playerCode);
        return playerMapper.toProfileResponse(player);
    }

    @Override
    @Transactional
    public PlayerProfileResponse updatePlayer(String playerCode, UpdatePlayerRequest request) {
        Player player = findPlayerByCode(playerCode);
        String email = normalizeEmail(request.email());

        if (ObjectUtil.nonNull(email)
                && ObjectUtil.isNotEqual(email, player.getEmail())
                && playerRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        player.setName(normalizeRequired(request.name()));
        player.setGameName(normalizeOptional(request.gameName()));
        player.setEmail(email);

        Player updatedPlayer = playerRepository.save(player);
        log.info("Updated player with code {}", updatedPlayer.getPlayerCode());
        return playerMapper.toProfileResponse(updatedPlayer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlayerSearchResponse> searchPlayers(String query) {
        String normalizedQuery = normalizeOptional(query);
        if (ObjectUtil.isNull(normalizedQuery)) {
            return List.of();
        }

        Pageable limit = PageRequest.of(0, SEARCH_LIMIT);
        return playerRepository.searchPlayers(normalizedQuery, limit)
                .stream()
                .map(playerMapper::toSearchResponse)
                .toList();
    }

    private Player findPlayerByCode(String playerCode) {
        String normalizedPlayerCode = normalizeRequired(playerCode).toUpperCase();
        return playerRepository.findByPlayerCode(normalizedPlayerCode)
                .orElseThrow(() -> new PlayerNotFoundException(normalizedPlayerCode));
    }

    private String normalizeRequired(String value) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException("Required value must not be blank");
        }
        return value.trim();
    }

    private String normalizeOptional(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String normalizeEmail(String email) {
        String normalizedEmail = normalizeOptional(email);
        return ObjectUtil.isNull(normalizedEmail) ? null : normalizedEmail.toLowerCase(Locale.ROOT);
    }
}
