UPDATE player_lifetime_stats
SET tournaments_played = (
    SELECT COUNT(*)
    FROM player_tournament_stats pts
    WHERE pts.player_id = player_lifetime_stats.player_id
);
