package com.amigos.courtpulse.repository;

import com.amigos.courtpulse.entity.TournamentMetadata;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TournamentMetadataRepository extends JpaRepository<TournamentMetadata, Long> {

    Optional<TournamentMetadata> findByTournamentCode(String code);

    List<TournamentMetadata> findAllByClubId(Long clubId);

    List<TournamentMetadata> findAllByClubIdOrderByTournamentDateDesc(Long clubId);

    boolean existsByTournamentCode(String code);
}
