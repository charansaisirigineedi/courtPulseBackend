package com.amigos.courtpulse.mapper;

import com.amigos.courtpulse.dto.player.PlayerProfileResponse;
import com.amigos.courtpulse.dto.player.PlayerSearchResponse;
import com.amigos.courtpulse.entity.Player;
import org.springframework.stereotype.Component;

@Component
public class PlayerMapper {

    public PlayerProfileResponse toProfileResponse(Player player) {
        return new PlayerProfileResponse(
                player.getPlayerCode(),
                player.getUsername(),
                player.getName(),
                player.getGameName(),
                player.getEmail(),
                player.isEmailVerified(),
                player.getStatus(),
                player.getCreatedAt()
        );
    }

    public PlayerSearchResponse toSearchResponse(Player player) {
        return new PlayerSearchResponse(
                player.getPlayerCode(),
                player.getName(),
                player.getGameName()
        );
    }
}
