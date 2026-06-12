package com.amigos.courtpulse.repository;

import com.amigos.courtpulse.entity.TournamentMatch;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TournamentMatchRepository extends JpaRepository<TournamentMatch, Long> {

    @Query("""
            SELECT m
            FROM TournamentMatch m
            JOIN FETCH m.tournament t
            JOIN FETCH t.club
            WHERE m.id = :matchId
            """)
    Optional<TournamentMatch> findByIdWithTournament(@Param("matchId") Long matchId);
}
