package com.amigos.courtpulse.exception;

public class PlayerNotFoundException extends RuntimeException {

    public PlayerNotFoundException(String playerCode) {
        super("Player not found with code: " + playerCode);
    }

    public PlayerNotFoundException(Long playerId) {
        super("Player not found with ID: " + playerId);
    }
}
