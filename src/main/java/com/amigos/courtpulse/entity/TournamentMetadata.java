package com.amigos.courtpulse.entity;

import com.amigos.courtpulse.enums.TournamentStatusEnum;
import com.amigos.courtpulse.enums.TournamentTypeEnum;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tournament_metadata")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TournamentMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tournament_code", nullable = false, unique = true, updatable = false, length = 10)
    private String tournamentCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "club_id", nullable = false, updatable = false)
    private Club club;

    @Column(name = "tournament_name", nullable = false, length = 100)
    @Setter
    private String tournamentName;

    @Column(length = 500)
    @Setter
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "tournament_type", nullable = false, length = 30)
    @Setter
    private TournamentTypeEnum tournamentType;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Setter
    private TournamentStatusEnum status = TournamentStatusEnum.DRAFT;

    @Column(name = "tournament_date", nullable = false)
    @Setter
    private LocalDate tournamentDate;

    @Column(name = "registration_start_at")
    @Setter
    private LocalDateTime registrationStartAt;

    @Column(name = "registration_end_at")
    @Setter
    private LocalDateTime registrationEndAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    private Player createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (ObjectUtil.isNull(createdAt)) {
            createdAt = now;
        }
        if (ObjectUtil.isNull(updatedAt)) {
            updatedAt = now;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
