package com.amigos.courtpulse.dto.tournament;

import com.amigos.courtpulse.enums.TournamentStatusEnum;
import com.amigos.courtpulse.enums.TournamentTypeEnum;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TournamentResponse(
        Long id,
        String tournamentCode,
        Long clubId,
        String tournamentName,
        String description,
        TournamentTypeEnum tournamentType,
        TournamentStatusEnum status,
        LocalDate tournamentDate,
        Long createdById,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
