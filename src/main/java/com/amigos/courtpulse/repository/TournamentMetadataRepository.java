package com.amigos.courtpulse.repository;

import com.amigos.courtpulse.entity.TournamentMetadata;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TournamentMetadataRepository extends JpaRepository<TournamentMetadata, Long> {

    @Query("""
            SELECT t
            FROM TournamentMetadata t
            JOIN FETCH t.club
            WHERE t.id = :id
            """)
    Optional<TournamentMetadata> findByIdWithClub(@Param("id") Long id);

    Optional<TournamentMetadata> findByTournamentCode(String code);

    List<TournamentMetadata> findAllByClubId(Long clubId);

    List<TournamentMetadata> findAllByClubIdOrderByTournamentDateDesc(Long clubId);

    boolean existsByTournamentCode(String code);
}
