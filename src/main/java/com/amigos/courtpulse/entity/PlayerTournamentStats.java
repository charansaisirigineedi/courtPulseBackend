package com.amigos.courtpulse.entity;

import com.amigos.courtpulse.enums.RoundNameEnum;
import com.amigos.courtpulse.enums.TournamentPlacementEnum;
import com.amigos.courtpulse.util.ObjectUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "player_tournament_stats")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerTournamentStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false, updatable = false)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tournament_id", nullable = false, updatable = false)
    private TournamentMetadata tournament;

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
    @Column(name = "points_scored", nullable = false)
    @Setter
    private int pointsScored = 0;

    @Builder.Default
    @Column(name = "points_conceded", nullable = false)
    @Setter
    private int pointsConceded = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "best_round_reached", length = 30)
    @Setter
    private RoundNameEnum bestRoundReached;

    @Enumerated(EnumType.STRING)
    @Column(name = "tournament_placement", length = 30)
    @Setter
    private TournamentPlacementEnum tournamentPlacement;

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
