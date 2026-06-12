package com.amigos.courtpulse.entity;

import com.amigos.courtpulse.enums.PlayerSideEnum;
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

@Entity
@Table(name = "tournament_match_player")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TournamentMatchPlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tournament_match_id", nullable = false, updatable = false)
    private TournamentMatch tournamentMatch;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false, updatable = false)
    private Player player;

    @Enumerated(EnumType.STRING)
    @Column(name = "side", nullable = false, length = 10)
    private PlayerSideEnum side;

    @Column(name = "is_winner", nullable = false)
    private boolean isWinner;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (ObjectUtil.isNull(createdAt)) {
            createdAt = LocalDateTime.now();
        }
    }
}
