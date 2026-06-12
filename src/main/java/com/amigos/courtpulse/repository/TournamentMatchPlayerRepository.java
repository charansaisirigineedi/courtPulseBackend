package com.amigos.courtpulse.repository;

import com.amigos.courtpulse.entity.TournamentMatchPlayer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TournamentMatchPlayerRepository extends JpaRepository<TournamentMatchPlayer, Long> {
}
