package com.amigos.courtpulse.entity;

import com.amigos.courtpulse.enums.PlayerStatusEnum;
import com.amigos.courtpulse.util.ObjectUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "player")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "player_code", nullable = false, unique = true, updatable = false, length = 10)
    private String playerCode;

    @Column(nullable = false, unique = true, updatable = false, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false)
    @Setter
    private String passwordHash;

    @Column(nullable = false, length = 100)
    @Setter
    private String name;

    @Column(name = "game_name", length = 100)
    @Setter
    private String gameName;

    @Column(unique = true)
    @Setter
    private String email;

    @Builder.Default
    @Column(name = "email_verified", nullable = false)
    @Setter
    private boolean emailVerified = false;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Setter
    private PlayerStatusEnum status = PlayerStatusEnum.ACTIVE;

    @Column(name = "last_login_at")
    @Setter
    private LocalDateTime lastLoginAt;

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
