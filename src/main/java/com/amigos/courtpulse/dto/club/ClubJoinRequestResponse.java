package com.amigos.courtpulse.dto.club;

import com.amigos.courtpulse.enums.ClubJoinRequestStatusEnum;
import java.time.LocalDateTime;

public record ClubJoinRequestResponse(
        Long requestId,
        String clubCode,
        String clubName,
        String playerCode,
        String playerName,
        ClubJoinRequestStatusEnum status,
        String reviewedByPlayerCode,
        LocalDateTime reviewedAt,
        LocalDateTime createdAt
) {
}
