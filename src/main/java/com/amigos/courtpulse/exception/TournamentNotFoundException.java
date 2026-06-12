package com.amigos.courtpulse.exception;

public class TournamentNotFoundException extends RuntimeException {

    public TournamentNotFoundException(Long tournamentId) {
        super("Tournament not found with id: " + tournamentId);
    }

    public TournamentNotFoundException(String tournamentCode) {
        super("Tournament not found with code: " + tournamentCode);
    }
}
