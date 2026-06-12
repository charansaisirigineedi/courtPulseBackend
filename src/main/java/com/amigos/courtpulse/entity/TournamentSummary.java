package com.amigos.courtpulse.entity;

import com.amigos.courtpulse.enums.TournamentSummaryStatusEnum;
import com.amigos.courtpulse.util.ObjectUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "tournament_summary")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TournamentSummary {

    @Id
    @Column(name = "tournament_id")
    private Long tournamentId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tournament_id", nullable = false, updatable = false)
    private TournamentMetadata tournament;

    @Builder.Default
    @Column(name = "total_matches_played", nullable = false)
    @Setter
    private int totalMatchesPlayed = 0;

    @Builder.Default
    @Column(name = "total_points_scored", nullable = false)
    @Setter
    private int totalPointsScored = 0;

    @Builder.Default
    @Column(name = "participant_count", nullable = false)
    @Setter
    private int participantCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "champion_player_id")
    @Setter
    private Player championPlayer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "runner_up_player_id")
    @Setter
    private Player runnerUpPlayer;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Setter
    private TournamentSummaryStatusEnum status = TournamentSummaryStatusEnum.IN_PROGRESS;

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
