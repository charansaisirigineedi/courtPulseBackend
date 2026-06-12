package com.amigos.courtpulse.dto.tournament;

import com.amigos.courtpulse.enums.RoundNameEnum;
import com.amigos.courtpulse.enums.WinnerSideEnum;
import com.amigos.courtpulse.enums.PlayerSideEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

public record SyncMatchResultRequest(
        @NotNull(message = "Round name is required")
        RoundNameEnum roundName,

        @NotNull(message = "Match number is required")
        Integer matchNo,

        @NotNull(message = "Score A is required")
        @PositiveOrZero(message = "Score A must be positive or zero")
        Integer scoreA,

        @NotNull(message = "Score B is required")
        @PositiveOrZero(message = "Score B must be positive or zero")
        Integer scoreB,

        @NotNull(message = "Winner side is required")
        WinnerSideEnum winnerSide,

        @NotNull(message = "Completed timestamp is required")
        LocalDateTime completedAt,

        @NotEmpty(message = "Players list must not be empty")
        @Valid
        List<MatchPlayerRequest> players
) {
    public record MatchPlayerRequest(
            @NotNull(message = "Player ID is required")
            Long playerId,

            @NotNull(message = "Player side is required")
            PlayerSideEnum side,

            @NotNull(message = "Winner flag is required")
            Boolean winner
    ) {
    }
}
