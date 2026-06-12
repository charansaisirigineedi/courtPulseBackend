package com.amigos.courtpulse.exception;

public class ClubJoinRequestAlreadyReviewedException extends RuntimeException {

    public ClubJoinRequestAlreadyReviewedException(Long requestId) {
        super("Club join request already reviewed with id: " + requestId);
    }
}
