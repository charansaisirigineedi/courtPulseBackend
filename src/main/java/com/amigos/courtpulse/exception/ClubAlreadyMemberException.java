package com.amigos.courtpulse.exception;

public class ClubAlreadyMemberException extends RuntimeException {

    public ClubAlreadyMemberException(String playerCode, String clubCode) {
        super("Player " + playerCode + " is already a member of club " + clubCode);
    }
}
