package com.amigos.courtpulse.exception;

public class ClubJoinRequestNotFoundException extends RuntimeException {

    public ClubJoinRequestNotFoundException(Long requestId) {
        super("Club join request not found with id: " + requestId);
    }
}
