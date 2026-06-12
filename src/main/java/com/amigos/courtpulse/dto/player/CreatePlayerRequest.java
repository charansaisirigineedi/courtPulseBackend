package com.amigos.courtpulse.dto.player;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePlayerRequest(
        @NotBlank
        @Size(max = 50)
        String username,

        @NotBlank
        @Size(max = 72)
        String password,

        @NotBlank
        @Size(max = 100)
        String name,

        @Size(max = 100)
        String gameName,

        @Email
        @Size(max = 255)
        String email
) {
}
