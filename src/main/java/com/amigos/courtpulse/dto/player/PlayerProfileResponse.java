package com.amigos.courtpulse.dto.player;

import com.amigos.courtpulse.enums.PlayerStatusEnum;
import java.time.LocalDateTime;

public record PlayerProfileResponse(
        String playerCode,
        String username,
        String name,
        String gameName,
        String email,
        boolean emailVerified,
        PlayerStatusEnum status,
        LocalDateTime createdAt
) {
}
