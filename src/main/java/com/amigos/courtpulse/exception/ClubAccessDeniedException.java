package com.amigos.courtpulse.exception;

public class ClubAccessDeniedException extends RuntimeException {

    public ClubAccessDeniedException(String playerCode, String clubCode) {
        super("Player " + playerCode + " is not allowed to manage club " + clubCode);
    }

    public ClubAccessDeniedException(String playerCode, Long clubId) {
        super("Player " + playerCode + " is not allowed to manage club ID: " + clubId);
    }
}
