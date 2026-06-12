package com.amigos.courtpulse.repository;

import com.amigos.courtpulse.entity.TournamentMatchPlayer;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TournamentMatchPlayerRepository extends JpaRepository<TournamentMatchPlayer, Long> {

    @Query("""
            SELECT mp
            FROM TournamentMatchPlayer mp
            JOIN FETCH mp.player
            WHERE mp.tournamentMatch.id = :matchId
            """)
    List<TournamentMatchPlayer> findByMatchIdWithPlayer(@Param("matchId") Long matchId);

    @Query(value = """
            SELECT partner.player_id AS partnerId,
                   COUNT(*) AS matchesTogether,
                   SUM(CASE WHEN self.is_winner THEN 1 ELSE 0 END) AS winsTogether
            FROM tournament_match_player self
            JOIN tournament_match tm ON self.tournament_match_id = tm.id
            JOIN tournament_metadata tmd ON tm.tournament_id = tmd.id
            JOIN tournament_match_player partner
                ON partner.tournament_match_id = tm.id
               AND partner.side = self.side
               AND partner.player_id <> self.player_id
            WHERE self.player_id = :playerId
              AND tmd.tournament_type IN ('FIXED_DOUBLES', 'RANDOM_DOUBLES')
            GROUP BY partner.player_id
            HAVING COUNT(*) >= :minMatches
            ORDER BY winsTogether DESC, matchesTogether DESC
            LIMIT 1
            """, nativeQuery = true)
    List<Object[]> findBestDoublesPartner(@Param("playerId") Long playerId, @Param("minMatches") int minMatches);
}
