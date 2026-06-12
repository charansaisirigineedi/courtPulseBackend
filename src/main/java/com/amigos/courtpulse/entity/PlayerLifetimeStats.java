package com.amigos.courtpulse.entity;

import com.amigos.courtpulse.util.ObjectUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "player_lifetime_stats")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerLifetimeStats {

    @Id
    @Column(name = "player_id")
    private Long playerId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false, updatable = false)
    private Player player;

    @Builder.Default
    @Column(name = "matches_played", nullable = false)
    @Setter
    private int matchesPlayed = 0;

    @Builder.Default
    @Column(name = "matches_won", nullable = false)
    @Setter
    private int matchesWon = 0;

    @Builder.Default
    @Column(name = "matches_lost", nullable = false)
    @Setter
    private int matchesLost = 0;

    @Builder.Default
    @Column(name = "matches_drawn", nullable = false)
    @Setter
    private int matchesDrawn = 0;

    @Builder.Default
    @Column(name = "singles_matches_played", nullable = false)
    @Setter
    private int singlesMatchesPlayed = 0;

    @Builder.Default
    @Column(name = "singles_matches_won", nullable = false)
    @Setter
    private int singlesMatchesWon = 0;

    @Builder.Default
    @Column(name = "singles_matches_lost", nullable = false)
    @Setter
    private int singlesMatchesLost = 0;

    @Builder.Default
    @Column(name = "doubles_matches_played", nullable = false)
    @Setter
    private int doublesMatchesPlayed = 0;

    @Builder.Default
    @Column(name = "doubles_matches_won", nullable = false)
    @Setter
    private int doublesMatchesWon = 0;

    @Builder.Default
    @Column(name = "doubles_matches_lost", nullable = false)
    @Setter
    private int doublesMatchesLost = 0;

    @Builder.Default
    @Column(name = "league_matches_played", nullable = false)
    @Setter
    private int leagueMatchesPlayed = 0;

    @Builder.Default
    @Column(name = "league_matches_won", nullable = false)
    @Setter
    private int leagueMatchesWon = 0;

    @Builder.Default
    @Column(name = "knockout_matches_played", nullable = false)
    @Setter
    private int knockoutMatchesPlayed = 0;

    @Builder.Default
    @Column(name = "knockout_matches_won", nullable = false)
    @Setter
    private int knockoutMatchesWon = 0;

    @Builder.Default
    @Column(name = "total_points_scored", nullable = false)
    @Setter
    private int totalPointsScored = 0;

    @Builder.Default
    @Column(name = "total_points_conceded", nullable = false)
    @Setter
    private int totalPointsConceded = 0;

    @Builder.Default
    @Column(name = "tournament_wins", nullable = false)
    @Setter
    private int tournamentWins = 0;

    @Builder.Default
    @Column(name = "tournament_runner_up", nullable = false)
    @Setter
    private int tournamentRunnerUp = 0;

    @Builder.Default
    @Column(name = "dominant_match_wins", nullable = false)
    @Setter
    private int dominantMatchWins = 0;

    @Builder.Default
    @Column(name = "current_win_streak", nullable = false)
    @Setter
    private int currentWinStreak = 0;

    @Builder.Default
    @Column(name = "best_win_streak", nullable = false)
    @Setter
    private int bestWinStreak = 0;

    @Column(name = "last_match_at")
    @Setter
    private LocalDateTime lastMatchAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        if (ObjectUtil.isNull(updatedAt)) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
