package com.amigos.courtpulse.dto.club;

public record LeaveClubResponse(
        String clubCode,
        String playerCode,
        String status
) {
}
