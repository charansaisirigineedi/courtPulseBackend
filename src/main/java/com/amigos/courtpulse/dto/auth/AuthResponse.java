package com.amigos.courtpulse.dto.auth;

import java.time.Instant;

public record AuthResponse(
        String accessToken,
        String tokenType,
        Instant expiresAt,
        AuthenticatedPlayerResponse player
) {
}
