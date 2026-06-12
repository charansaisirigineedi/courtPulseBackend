package com.amigos.courtpulse.service;

import com.amigos.courtpulse.dto.tournament.CreateTournamentRequest;
import com.amigos.courtpulse.dto.tournament.CreateTournamentResponse;
import com.amigos.courtpulse.dto.tournament.TournamentResponse;
import java.util.List;

public interface TournamentService {
    CreateTournamentResponse createTournament(CreateTournamentRequest request, String playerCode);
    TournamentResponse getTournamentById(Long id);
    List<TournamentResponse> getTournamentsByClubIdentifier(String clubIdentifier);
}
