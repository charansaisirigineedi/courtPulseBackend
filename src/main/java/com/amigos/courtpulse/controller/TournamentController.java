package com.amigos.courtpulse.controller;

import com.amigos.courtpulse.dto.analytics.TournamentSummaryResponse;
import com.amigos.courtpulse.dto.common.ApiResponse;
import com.amigos.courtpulse.dto.tournament.CreateTournamentRequest;
import com.amigos.courtpulse.dto.tournament.CreateTournamentResponse;
import com.amigos.courtpulse.dto.tournament.SyncMatchResultRequest;
import com.amigos.courtpulse.dto.tournament.SyncMatchResultResponse;
import com.amigos.courtpulse.dto.tournament.TournamentResponse;
import com.amigos.courtpulse.service.PlayerAnalyticsService;
import com.amigos.courtpulse.service.TournamentService;
import com.amigos.courtpulse.service.TournamentMatchService;
import com.amigos.courtpulse.util.ResponseUtil;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
public class TournamentController {

    private final TournamentService tournamentService;
    private final TournamentMatchService tournamentMatchService;
    private final PlayerAnalyticsService playerAnalyticsService;

    @PostMapping("/api/v1/tournaments")
    public ResponseEntity<ApiResponse<CreateTournamentResponse>> createTournament(
            @Valid @RequestBody CreateTournamentRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        CreateTournamentResponse response = tournamentService.createTournament(request, playerCode(jwt));
        URI location = URI.create("/api/v1/tournaments/" + response.id());
        return ResponseUtil.created("Tournament created successfully", response, location);
    }

    @GetMapping("/api/v1/tournaments/{id}")
    public ResponseEntity<ApiResponse<TournamentResponse>> getTournamentById(
            @PathVariable Long id
    ) {
        TournamentResponse response = tournamentService.getTournamentById(id);
        return ResponseUtil.ok("Tournament fetched successfully", response);
    }

    @GetMapping("/api/v1/clubs/{clubId}/tournaments")
    public ResponseEntity<ApiResponse<List<TournamentResponse>>> getClubTournaments(
            @PathVariable("clubId") String clubId
    ) {
        List<TournamentResponse> response = tournamentService.getTournamentsByClubIdentifier(clubId);
        return ResponseUtil.ok("Club tournaments fetched successfully", response);
    }

    @PostMapping("/api/v1/tournaments/{tournamentId}/matches")
    public ResponseEntity<ApiResponse<SyncMatchResultResponse>> syncMatchResult(
            @PathVariable Long tournamentId,
            @Valid @RequestBody SyncMatchResultRequest request
    ) {
        SyncMatchResultResponse response = tournamentMatchService.syncMatchResult(tournamentId, request);
        return ResponseUtil.ok("Match result synchronized successfully", response);
    }

    @GetMapping("/api/v1/tournaments/{tournamentId}/summary")
    public ResponseEntity<ApiResponse<TournamentSummaryResponse>> getTournamentSummary(
            @PathVariable Long tournamentId
    ) {
        TournamentSummaryResponse response = playerAnalyticsService.getTournamentSummary(tournamentId);
        return ResponseUtil.ok("Tournament summary fetched successfully", response);
    }

    private String playerCode(Jwt jwt) {
        return jwt.getClaimAsString("playerCode");
    }
}
