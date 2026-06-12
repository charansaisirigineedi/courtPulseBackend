package com.amigos.courtpulse.repository;

import com.amigos.courtpulse.entity.PlayerClubStats;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlayerClubStatsRepository extends JpaRepository<PlayerClubStats, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT s
            FROM PlayerClubStats s
            WHERE s.player.id = :playerId
              AND s.club.id = :clubId
            """)
    Optional<PlayerClubStats> findByPlayerIdAndClubIdForUpdate(
            @Param("playerId") Long playerId,
            @Param("clubId") Long clubId
    );

    Optional<PlayerClubStats> findByPlayer_PlayerCodeAndClub_ClubCode(String playerCode, String clubCode);
}
