package com.amigos.courtpulse.entity;

import com.amigos.courtpulse.util.ObjectUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "player_club_stats")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerClubStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false, updatable = false)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "club_id", nullable = false, updatable = false)
    private Club club;

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
