package com.amigos.courtpulse.entity;

import com.amigos.courtpulse.enums.RoundNameEnum;
import com.amigos.courtpulse.enums.WinnerSideEnum;
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
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tournament_match")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TournamentMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tournament_id", nullable = false, updatable = false)
    private TournamentMetadata tournament;

    @Enumerated(EnumType.STRING)
    @Column(name = "round_name", nullable = false, length = 30)
    private RoundNameEnum roundName;

    @Column(name = "match_no", nullable = false)
    private Integer matchNo;

    @Column(name = "score_a", nullable = false)
    private Integer scoreA;

    @Column(name = "score_b", nullable = false)
    private Integer scoreB;

    @Enumerated(EnumType.STRING)
    @Column(name = "winner_side", nullable = false, length = 10)
    private WinnerSideEnum winnerSide;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    @Setter
    @Column(name = "analytics_processed_at")
    private LocalDateTime analyticsProcessedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (ObjectUtil.isNull(createdAt)) {
            createdAt = LocalDateTime.now();
        }
    }
}
