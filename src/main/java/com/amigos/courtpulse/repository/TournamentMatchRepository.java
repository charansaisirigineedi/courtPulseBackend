package com.amigos.courtpulse.repository;

import com.amigos.courtpulse.entity.TournamentMatch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TournamentMatchRepository extends JpaRepository<TournamentMatch, Long> {
}
