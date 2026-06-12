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

    @Query(value = """
            SELECT COUNT(DISTINCT tm.id) AS matches_played,
                   SUM(CASE
                       WHEN tm.winner_side = 'DRAW' THEN 0
                       WHEN (p1.side = 'SIDE_A' AND tm.winner_side = 'SIDE_A')
                         OR (p1.side = 'SIDE_B' AND tm.winner_side = 'SIDE_B') THEN 1
                       ELSE 0
                   END) AS team_a_wins,
                   SUM(CASE
                       WHEN tm.winner_side = 'DRAW' THEN 0
                       WHEN (p1.side = 'SIDE_A' AND tm.winner_side = 'SIDE_B')
                         OR (p1.side = 'SIDE_B' AND tm.winner_side = 'SIDE_A') THEN 1
                       ELSE 0
                   END) AS team_b_wins,
                   SUM(CASE WHEN tm.winner_side = 'DRAW' THEN 1 ELSE 0 END) AS draws
            FROM tournament_match tm
            JOIN tournament_match_player p1 ON p1.tournament_match_id = tm.id AND p1.player_id = :teamAPlayerId
            JOIN tournament_match_player p2 ON p2.tournament_match_id = tm.id AND p2.player_id = :teamBPlayerId
            WHERE p1.side <> p2.side
            """, nativeQuery = true)
    List<Object[]> findSinglesHeadToHead(
            @Param("teamAPlayerId") Long teamAPlayerId,
            @Param("teamBPlayerId") Long teamBPlayerId
    );

    @Query(value = """
            SELECT COUNT(DISTINCT tm.id) AS matches_played,
                   SUM(CASE
                       WHEN tm.winner_side = 'DRAW' THEN 0
                       WHEN tm.winner_side = team_a_mp.side THEN 1
                       ELSE 0
                   END) AS team_a_wins,
                   SUM(CASE
                       WHEN tm.winner_side = 'DRAW' THEN 0
                       WHEN tm.winner_side <> team_a_mp.side THEN 1
                       ELSE 0
                   END) AS team_b_wins,
                   SUM(CASE WHEN tm.winner_side = 'DRAW' THEN 1 ELSE 0 END) AS draws
            FROM tournament_match tm
            JOIN tournament_match_player team_a_mp
                ON team_a_mp.tournament_match_id = tm.id
               AND team_a_mp.player_id = :teamAPlayerOneId
            WHERE (
                SELECT COUNT(DISTINCT mp.player_id)
                FROM tournament_match_player mp
                WHERE mp.tournament_match_id = tm.id
                  AND mp.side = team_a_mp.side
                  AND mp.player_id IN (:teamAPlayerOneId, :teamAPlayerTwoId)
            ) = 2
            AND (
                SELECT COUNT(DISTINCT mp.player_id)
                FROM tournament_match_player mp
                WHERE mp.tournament_match_id = tm.id
                  AND mp.side <> team_a_mp.side
                  AND mp.player_id IN (:teamBPlayerOneId, :teamBPlayerTwoId)
            ) = 2
            AND (
                SELECT COUNT(*)
                FROM tournament_match_player mp
                WHERE mp.tournament_match_id = tm.id
                  AND mp.side = team_a_mp.side
                  AND mp.player_id NOT IN (:teamAPlayerOneId, :teamAPlayerTwoId)
            ) = 0
            AND (
                SELECT COUNT(*)
                FROM tournament_match_player mp
                WHERE mp.tournament_match_id = tm.id
                  AND mp.side <> team_a_mp.side
                  AND mp.player_id NOT IN (:teamBPlayerOneId, :teamBPlayerTwoId)
            ) = 0
            """, nativeQuery = true)
    List<Object[]> findDoublesHeadToHead(
            @Param("teamAPlayerOneId") Long teamAPlayerOneId,
            @Param("teamAPlayerTwoId") Long teamAPlayerTwoId,
            @Param("teamBPlayerOneId") Long teamBPlayerOneId,
            @Param("teamBPlayerTwoId") Long teamBPlayerTwoId
    );

    @Query(value = """
            SELECT COUNT(*) AS matches_together,
                   SUM(CASE WHEN self_mp.is_winner THEN 1 ELSE 0 END) AS wins_together
            FROM tournament_match_player self_mp
            JOIN tournament_match tm ON self_mp.tournament_match_id = tm.id
            JOIN tournament_match_player partner_mp
                ON partner_mp.tournament_match_id = tm.id
               AND partner_mp.side = self_mp.side
               AND partner_mp.player_id <> self_mp.player_id
            WHERE self_mp.player_id = :playerOneId
              AND partner_mp.player_id = :playerTwoId
            """, nativeQuery = true)
    List<Object[]> findPartnershipStats(
            @Param("playerOneId") Long playerOneId,
            @Param("playerTwoId") Long playerTwoId
    );
}
