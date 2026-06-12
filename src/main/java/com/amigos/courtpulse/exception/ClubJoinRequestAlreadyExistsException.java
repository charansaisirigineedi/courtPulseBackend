package com.amigos.courtpulse.exception;

public class ClubJoinRequestAlreadyExistsException extends RuntimeException {

    public ClubJoinRequestAlreadyExistsException(String playerCode, String clubCode) {
        super("Join request already exists for player " + playerCode + " and club " + clubCode);
    }
}
