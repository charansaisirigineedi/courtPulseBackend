package com.amigos.courtpulse.dto.club;

import com.amigos.courtpulse.enums.ClubJoinRequestStatusEnum;
import java.time.LocalDateTime;

public record MyClubJoinRequestResponse(
        Long requestId,
        String clubCode,
        String clubName,
        ClubJoinRequestStatusEnum status,
        LocalDateTime createdAt
) {
}
