package com.amigos.courtpulse.service;

import com.amigos.courtpulse.dto.tournament.SyncMatchResultRequest;
import com.amigos.courtpulse.dto.tournament.SyncMatchResultResponse;

public interface TournamentMatchService {
    SyncMatchResultResponse syncMatchResult(Long tournamentId, SyncMatchResultRequest request);
}
