package com.amigos.courtpulse.repository;

import com.amigos.courtpulse.entity.TournamentSummary;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TournamentSummaryRepository extends JpaRepository<TournamentSummary, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM TournamentSummary s WHERE s.tournamentId = :tournamentId")
    Optional<TournamentSummary> findByTournamentIdForUpdate(@Param("tournamentId") Long tournamentId);

    Optional<TournamentSummary> findByTournamentId(Long tournamentId);

    @Query("""
            SELECT s
            FROM TournamentSummary s
            JOIN FETCH s.tournament
            LEFT JOIN FETCH s.championPlayer
            LEFT JOIN FETCH s.runnerUpPlayer
            WHERE s.tournamentId = :tournamentId
            """)
    Optional<TournamentSummary> findByTournamentIdWithDetails(@Param("tournamentId") Long tournamentId);

    @Query("""
            SELECT s
            FROM TournamentSummary s
            JOIN FETCH s.tournament t
            LEFT JOIN FETCH s.championPlayer
            WHERE UPPER(t.club.clubCode) = UPPER(:clubCode)
              AND s.status = com.amigos.courtpulse.enums.TournamentSummaryStatusEnum.COMPLETED
            ORDER BY t.tournamentDate DESC
            """)
    List<TournamentSummary> findRecentCompletedByClubCode(
            @Param("clubCode") String clubCode,
            Pageable pageable
    );
}
