package com.amigos.courtpulse.dto.tournament;

import com.amigos.courtpulse.enums.TournamentStatusEnum;

public record CreateTournamentResponse(
        Long id,
        String tournamentCode,
        TournamentStatusEnum status
) {
}
