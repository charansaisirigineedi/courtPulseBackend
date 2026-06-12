package com.amigos.courtpulse.repository;

import com.amigos.courtpulse.entity.PlayerTournamentStats;
import com.amigos.courtpulse.enums.TournamentPlacementEnum;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlayerTournamentStatsRepository extends JpaRepository<PlayerTournamentStats, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT s
            FROM PlayerTournamentStats s
            WHERE s.player.id = :playerId
              AND s.tournament.id = :tournamentId
            """)
    Optional<PlayerTournamentStats> findByPlayerIdAndTournamentIdForUpdate(
            @Param("playerId") Long playerId,
            @Param("tournamentId") Long tournamentId
    );

    @Query("""
            SELECT s
            FROM PlayerTournamentStats s
            JOIN FETCH s.tournament t
            WHERE s.player.playerCode = :playerCode
              AND s.tournamentPlacement = com.amigos.courtpulse.enums.TournamentPlacementEnum.CHAMPION
            ORDER BY t.tournamentDate DESC
            """)
    List<PlayerTournamentStats> findRecentWinsByPlayerCode(
            @Param("playerCode") String playerCode,
            Pageable pageable
    );

    @Query("""
            SELECT s
            FROM PlayerTournamentStats s
            JOIN FETCH s.tournament t
            WHERE s.player.playerCode = :playerCode
              AND (:placement IS NULL OR s.tournamentPlacement = :placement)
            ORDER BY t.tournamentDate DESC
            """)
    List<PlayerTournamentStats> findByPlayerCodeWithTournament(
            @Param("playerCode") String playerCode,
            @Param("placement") TournamentPlacementEnum placement,
            Pageable pageable
    );

    @Query("""
            SELECT s
            FROM PlayerTournamentStats s
            JOIN FETCH s.tournament t
            WHERE s.player.playerCode = :playerCode
              AND s.tournament.id = :tournamentId
            """)
    Optional<PlayerTournamentStats> findByPlayerCodeAndTournamentId(
            @Param("playerCode") String playerCode,
            @Param("tournamentId") Long tournamentId
    );

    @Query("""
            SELECT s
            FROM PlayerTournamentStats s
            WHERE s.player.id IN :playerIds
              AND s.tournament.id = :tournamentId
            """)
    List<PlayerTournamentStats> findByPlayerIdsAndTournamentId(
            @Param("playerIds") List<Long> playerIds,
            @Param("tournamentId") Long tournamentId
    );
}
