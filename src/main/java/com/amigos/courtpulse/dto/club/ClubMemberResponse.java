package com.amigos.courtpulse.dto.club;

import com.amigos.courtpulse.enums.ClubMemberRoleEnum;
import java.time.LocalDateTime;

public record ClubMemberResponse(
        String clubCode,
        String playerCode,
        String playerName,
        ClubMemberRoleEnum role,
        LocalDateTime joinedAt
) {
}
