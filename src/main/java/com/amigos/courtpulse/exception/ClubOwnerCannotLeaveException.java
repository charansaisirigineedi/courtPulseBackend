package com.amigos.courtpulse.exception;

public class ClubOwnerCannotLeaveException extends RuntimeException {

    public ClubOwnerCannotLeaveException(String playerCode, String clubCode) {
        super("Club owner " + playerCode + " cannot leave club " + clubCode + " before ownership is transferred");
    }
}
