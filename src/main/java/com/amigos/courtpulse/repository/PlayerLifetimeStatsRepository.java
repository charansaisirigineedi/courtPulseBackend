package com.amigos.courtpulse.repository;

import com.amigos.courtpulse.entity.PlayerLifetimeStats;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlayerLifetimeStatsRepository extends JpaRepository<PlayerLifetimeStats, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM PlayerLifetimeStats s WHERE s.playerId = :playerId")
    Optional<PlayerLifetimeStats> findByPlayerIdForUpdate(@Param("playerId") Long playerId);

    Optional<PlayerLifetimeStats> findByPlayer_PlayerCode(String playerCode);
}
