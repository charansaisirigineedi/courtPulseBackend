package com.amigos.courtpulse.dto.club;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateClubRequest(
        @NotBlank
        @Size(max = 100)
        String clubName,

        @Size(max = 500)
        String description
) {
}
