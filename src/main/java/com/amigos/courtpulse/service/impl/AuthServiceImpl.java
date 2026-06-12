package com.amigos.courtpulse.service.impl;

import com.amigos.courtpulse.dto.auth.AuthResponse;
import com.amigos.courtpulse.dto.auth.AuthenticatedPlayerResponse;
import com.amigos.courtpulse.dto.auth.LoginRequest;
import com.amigos.courtpulse.entity.Player;
import com.amigos.courtpulse.exception.PlayerNotFoundException;
import com.amigos.courtpulse.repository.PlayerRepository;
import com.amigos.courtpulse.security.JwtTokenService;
import com.amigos.courtpulse.security.PlayerPrincipal;
import com.amigos.courtpulse.service.AuthService;
import java.time.LocalDateTime;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String TOKEN_TYPE = "Bearer";

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final PlayerRepository playerRepository;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username().trim(), request.password())
        );
        PlayerPrincipal principal = (PlayerPrincipal) authentication.getPrincipal();

        Player player = playerRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new PlayerNotFoundException(principal.getPlayerCode()));
        player.setLastLoginAt(LocalDateTime.now());

        JwtTokenService.GeneratedToken accessToken = jwtTokenService.generateAccessToken(principal);
        return new AuthResponse(
                accessToken.value(),
                TOKEN_TYPE,
                accessToken.expiresAt(),
                toAuthenticatedPlayerResponse(principal)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AuthenticatedPlayerResponse getCurrentPlayer(Jwt jwt) {
        String playerCode = jwt.getClaimAsString("playerCode");
        Player player = playerRepository.findByPlayerCode(playerCode.toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new PlayerNotFoundException(playerCode));

        return toAuthenticatedPlayerResponse(new PlayerPrincipal(player));
    }

    private AuthenticatedPlayerResponse toAuthenticatedPlayerResponse(PlayerPrincipal principal) {
        return new AuthenticatedPlayerResponse(
                principal.getPlayerCode(),
                principal.getUsername(),
                principal.getName(),
                principal.getGameName(),
                principal.getEmail(),
                principal.isEmailVerified(),
                principal.getStatus()
        );
    }
}
