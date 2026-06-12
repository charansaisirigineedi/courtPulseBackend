package com.amigos.courtpulse.dto.club;

import com.amigos.courtpulse.enums.ClubMemberRoleEnum;
import com.amigos.courtpulse.enums.ClubStatusEnum;
import java.time.LocalDateTime;

public record MyClubResponse(
        String clubCode,
        String clubName,
        String description,
        ClubMemberRoleEnum role,
        ClubStatusEnum status,
        LocalDateTime createdAt
) {
}
