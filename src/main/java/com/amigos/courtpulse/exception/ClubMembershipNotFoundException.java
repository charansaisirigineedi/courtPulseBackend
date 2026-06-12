package com.amigos.courtpulse.exception;

public class ClubMembershipNotFoundException extends RuntimeException {

    public ClubMembershipNotFoundException(String playerCode, String clubCode) {
        super("Player " + playerCode + " is not a member of club " + clubCode);
    }
}
