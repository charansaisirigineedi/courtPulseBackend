package com.amigos.courtpulse.dto.player;

public record PlayerSearchResponse(
        String playerCode,
        String name,
        String gameName
) {
}
