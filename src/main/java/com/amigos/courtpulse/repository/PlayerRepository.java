package com.amigos.courtpulse.repository;

import com.amigos.courtpulse.entity.Player;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    Optional<Player> findByPlayerCode(String playerCode);

    Optional<Player> findByUsername(String username);

    Optional<Player> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPlayerCode(String playerCode);

    @Query("""
            SELECT p
            FROM Player p
            WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(p.gameName) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(p.playerCode) LIKE LOWER(CONCAT('%', :query, '%'))
            ORDER BY p.name ASC, p.playerCode ASC
            """)
    List<Player> searchPlayers(@Param("query") String query, Pageable pageable);
}
