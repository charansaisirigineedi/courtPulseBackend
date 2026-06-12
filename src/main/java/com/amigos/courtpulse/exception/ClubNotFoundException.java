package com.amigos.courtpulse.exception;

public class ClubNotFoundException extends RuntimeException {

    public ClubNotFoundException(String clubCode) {
        super("Club not found with code: " + clubCode);
    }

    public ClubNotFoundException(Long clubId) {
        super("Club not found with ID: " + clubId);
    }
}
