package com.amigos.courtpulse.dto.auth;

import com.amigos.courtpulse.enums.PlayerStatusEnum;

public record AuthenticatedPlayerResponse(
        String playerCode,
        String username,
        String name,
        String gameName,
        String email,
        boolean emailVerified,
        PlayerStatusEnum status
) {
}
