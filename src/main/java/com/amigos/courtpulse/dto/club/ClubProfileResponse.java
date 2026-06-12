package com.amigos.courtpulse.dto.club;

import com.amigos.courtpulse.enums.ClubStatusEnum;
import java.time.LocalDateTime;

public record ClubProfileResponse(
        Long id,
        String clubCode,
        String clubName,
        String description,
        String ownerPlayerCode,
        String ownerName,
        ClubStatusEnum status,
        LocalDateTime createdAt
) {
}
