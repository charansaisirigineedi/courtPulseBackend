package com.amigos.courtpulse.mapper;

import com.amigos.courtpulse.dto.tournament.CreateTournamentResponse;
import com.amigos.courtpulse.dto.tournament.TournamentResponse;
import com.amigos.courtpulse.entity.TournamentMetadata;
import org.springframework.stereotype.Component;

@Component
public class TournamentMapper {

    public CreateTournamentResponse toCreateResponse(TournamentMetadata metadata) {
        return new CreateTournamentResponse(
                metadata.getId(),
                metadata.getTournamentCode(),
                metadata.getStatus()
        );
    }

    public TournamentResponse toResponse(TournamentMetadata metadata) {
        return new TournamentResponse(
                metadata.getId(),
                metadata.getTournamentCode(),
                metadata.getClub().getId(),
                metadata.getTournamentName(),
                metadata.getDescription(),
                metadata.getTournamentType(),
                metadata.getStatus(),
                metadata.getTournamentDate(),
                metadata.getCreatedBy().getId(),
                metadata.getCreatedAt(),
                metadata.getUpdatedAt()
        );
    }
}
