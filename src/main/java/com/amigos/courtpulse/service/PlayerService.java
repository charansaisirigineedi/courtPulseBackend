package com.amigos.courtpulse.service;

import com.amigos.courtpulse.dto.player.CreatePlayerRequest;
import com.amigos.courtpulse.dto.player.PlayerProfileResponse;
import com.amigos.courtpulse.dto.player.PlayerSearchResponse;
import com.amigos.courtpulse.dto.player.UpdatePlayerRequest;
import java.util.List;

public interface PlayerService {

    PlayerProfileResponse createPlayer(CreatePlayerRequest request);

    PlayerProfileResponse getPlayerByCode(String playerCode);

    PlayerProfileResponse updatePlayer(String playerCode, UpdatePlayerRequest request);

    List<PlayerSearchResponse> searchPlayers(String query);
}
