package com.amigos.courtpulse.controller;

import com.amigos.courtpulse.dto.analytics.PlayerClubAnalyticsResponse;
import com.amigos.courtpulse.dto.analytics.PlayerLifetimeAnalyticsResponse;
import com.amigos.courtpulse.dto.analytics.PlayerTournamentAnalyticsResponse;
import com.amigos.courtpulse.dto.common.ApiResponse;
import com.amigos.courtpulse.enums.TournamentPlacementEnum;
import com.amigos.courtpulse.dto.player.CreatePlayerRequest;
import com.amigos.courtpulse.dto.player.PlayerProfileResponse;
import com.amigos.courtpulse.dto.player.PlayerSearchResponse;
import com.amigos.courtpulse.dto.player.UpdatePlayerRequest;
import com.amigos.courtpulse.service.PlayerAnalyticsService;
import com.amigos.courtpulse.service.PlayerService;
import com.amigos.courtpulse.util.AnalyticsUtil;
import com.amigos.courtpulse.util.PaginationUtil;
import com.amigos.courtpulse.util.ResponseUtil;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;
    private final PlayerAnalyticsService playerAnalyticsService;

    @PostMapping
    public ResponseEntity<ApiResponse<PlayerProfileResponse>> createPlayer(
            @Valid @RequestBody CreatePlayerRequest request
    ) {
        PlayerProfileResponse response = playerService.createPlayer(request);
        URI location = URI.create("/api/v1/players/" + response.playerCode());
        return ResponseUtil.created("Player created successfully", response, location);
    }

    @GetMapping("/me/analytics")
    public ResponseEntity<ApiResponse<PlayerLifetimeAnalyticsResponse>> getMyLifetimeAnalytics(
            @AuthenticationPrincipal Jwt jwt
    ) {
        PlayerLifetimeAnalyticsResponse response = playerAnalyticsService.getLifetimeAnalytics(playerCode(jwt));
        return ResponseUtil.ok("Player analytics fetched successfully", response);
    }

    @GetMapping("/me/analytics/tournaments")
    public ResponseEntity<ApiResponse<List<PlayerTournamentAnalyticsResponse>>> getMyTournamentAnalytics(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(name = "placement", required = false) TournamentPlacementEnum placement,
            @RequestParam(name = "limit", defaultValue = "20") int limit,
            @RequestParam(name = "offset", defaultValue = "0") int offset
    ) {
        Pageable pageable = PaginationUtil.toOffsetPageable(
                limit,
                offset,
                AnalyticsUtil.defaultTournamentHistoryLimit(),
                AnalyticsUtil.maxTournamentHistoryLimit()
        );
        List<PlayerTournamentAnalyticsResponse> response =
                playerAnalyticsService.getTournamentAnalytics(playerCode(jwt), placement, pageable);
        return ResponseUtil.ok("Player tournament analytics fetched successfully", response);
    }

    @GetMapping("/me/analytics/tournaments/{tournamentId}")
    public ResponseEntity<ApiResponse<PlayerTournamentAnalyticsResponse>> getMyTournamentAnalyticsById(
            @PathVariable Long tournamentId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        PlayerTournamentAnalyticsResponse response =
                playerAnalyticsService.getPlayerTournamentAnalytics(playerCode(jwt), tournamentId);
        return ResponseUtil.ok("Player tournament analytics fetched successfully", response);
    }

    @GetMapping("/me/analytics/clubs/{clubCode}")
    public ResponseEntity<ApiResponse<PlayerClubAnalyticsResponse>> getMyClubAnalytics(
            @PathVariable String clubCode,
            @AuthenticationPrincipal Jwt jwt
    ) {
        PlayerClubAnalyticsResponse response =
                playerAnalyticsService.getClubAnalytics(playerCode(jwt), clubCode);
        return ResponseUtil.ok("Player club analytics fetched successfully", response);
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<PlayerSearchResponse>>> searchPlayers(
            @RequestParam(name = "q", defaultValue = "") String query
    ) {
        List<PlayerSearchResponse> response = playerService.searchPlayers(query);
        return ResponseUtil.ok("Players fetched successfully", response);
    }

    @GetMapping("/{playerCode}/analytics")
    public ResponseEntity<ApiResponse<PlayerLifetimeAnalyticsResponse>> getPlayerLifetimeAnalytics(
            @PathVariable String playerCode
    ) {
        PlayerLifetimeAnalyticsResponse response = playerAnalyticsService.getLifetimeAnalytics(playerCode);
        return ResponseUtil.ok("Player analytics fetched successfully", response);
    }

    @GetMapping("/{playerCode}")
    public ResponseEntity<ApiResponse<PlayerProfileResponse>> getPlayerByCode(
            @PathVariable String playerCode
    ) {
        PlayerProfileResponse response = playerService.getPlayerByCode(playerCode);
        return ResponseUtil.ok("Player profile fetched successfully", response);
    }

    @PutMapping("/{playerCode}")
    public ResponseEntity<ApiResponse<PlayerProfileResponse>> updatePlayer(
            @PathVariable String playerCode,
            @Valid @RequestBody UpdatePlayerRequest request
    ) {
        PlayerProfileResponse response = playerService.updatePlayer(playerCode, request);
        return ResponseUtil.ok("Player updated successfully", response);
    }

    private String playerCode(Jwt jwt) {
        return jwt.getClaimAsString("playerCode");
    }
}
