package com.amigos.courtpulse.controller;

import com.amigos.courtpulse.dto.common.ApiResponse;
import com.amigos.courtpulse.dto.player.CreatePlayerRequest;
import com.amigos.courtpulse.dto.player.PlayerProfileResponse;
import com.amigos.courtpulse.dto.player.PlayerSearchResponse;
import com.amigos.courtpulse.dto.player.UpdatePlayerRequest;
import com.amigos.courtpulse.service.PlayerService;
import com.amigos.courtpulse.util.ResponseUtil;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    @PostMapping
    public ResponseEntity<ApiResponse<PlayerProfileResponse>> createPlayer(
            @Valid @RequestBody CreatePlayerRequest request
    ) {
        PlayerProfileResponse response = playerService.createPlayer(request);
        URI location = URI.create("/api/v1/players/" + response.playerCode());
        return ResponseUtil.created("Player created successfully", response, location);
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

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<PlayerSearchResponse>>> searchPlayers(
            @RequestParam(name = "q", defaultValue = "") String query
    ) {
        List<PlayerSearchResponse> response = playerService.searchPlayers(query);
        return ResponseUtil.ok("Players fetched successfully", response);
    }
}
