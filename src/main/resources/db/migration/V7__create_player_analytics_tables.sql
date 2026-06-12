ALTER TABLE tournament_match
    ADD CONSTRAINT uq_tournament_match_tournament_round_match
        UNIQUE (tournament_id, round_name, match_no);

CREATE TABLE player_lifetime_stats (
    player_id BIGINT PRIMARY KEY,
    matches_played INT NOT NULL DEFAULT 0,
    matches_won INT NOT NULL DEFAULT 0,
    matches_lost INT NOT NULL DEFAULT 0,
    matches_drawn INT NOT NULL DEFAULT 0,
    singles_matches_played INT NOT NULL DEFAULT 0,
    singles_matches_won INT NOT NULL DEFAULT 0,
    singles_matches_lost INT NOT NULL DEFAULT 0,
    doubles_matches_played INT NOT NULL DEFAULT 0,
    doubles_matches_won INT NOT NULL DEFAULT 0,
    doubles_matches_lost INT NOT NULL DEFAULT 0,
    league_matches_played INT NOT NULL DEFAULT 0,
    league_matches_won INT NOT NULL DEFAULT 0,
    knockout_matches_played INT NOT NULL DEFAULT 0,
    knockout_matches_won INT NOT NULL DEFAULT 0,
    total_points_scored INT NOT NULL DEFAULT 0,
    total_points_conceded INT NOT NULL DEFAULT 0,
    tournaments_participated INT NOT NULL DEFAULT 0,
    tournament_wins INT NOT NULL DEFAULT 0,
    tournament_runner_up INT NOT NULL DEFAULT 0,
    tournament_semi_final_reached INT NOT NULL DEFAULT 0,
    tournament_quarter_final_reached INT NOT NULL DEFAULT 0,
    close_match_wins INT NOT NULL DEFAULT 0,
    dominant_match_wins INT NOT NULL DEFAULT 0,
    current_win_streak INT NOT NULL DEFAULT 0,
    best_win_streak INT NOT NULL DEFAULT 0,
    last_match_at TIMESTAMP NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_player_lifetime_stats_player
        FOREIGN KEY (player_id)
        REFERENCES player(id)
);

CREATE TABLE player_tournament_stats (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    player_id BIGINT NOT NULL,
    tournament_id BIGINT NOT NULL,
    matches_played INT NOT NULL DEFAULT 0,
    matches_won INT NOT NULL DEFAULT 0,
    matches_lost INT NOT NULL DEFAULT 0,
    matches_drawn INT NOT NULL DEFAULT 0,
    points_scored INT NOT NULL DEFAULT 0,
    points_conceded INT NOT NULL DEFAULT 0,
    best_round_reached VARCHAR(30) NULL,
    tournament_placement VARCHAR(30) NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_player_tournament_stats_player
        FOREIGN KEY (player_id)
        REFERENCES player(id),
    CONSTRAINT fk_player_tournament_stats_tournament
        FOREIGN KEY (tournament_id)
        REFERENCES tournament_metadata(id),
    CONSTRAINT uq_player_tournament_stats_player_tournament
        UNIQUE (player_id, tournament_id)
);

CREATE INDEX idx_player_tournament_stats_player_id ON player_tournament_stats(player_id);
CREATE INDEX idx_player_tournament_stats_tournament_id ON player_tournament_stats(tournament_id);

CREATE TABLE player_club_stats (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    player_id BIGINT NOT NULL,
    club_id BIGINT NOT NULL,
    matches_played INT NOT NULL DEFAULT 0,
    matches_won INT NOT NULL DEFAULT 0,
    matches_lost INT NOT NULL DEFAULT 0,
    matches_drawn INT NOT NULL DEFAULT 0,
    total_points_scored INT NOT NULL DEFAULT 0,
    total_points_conceded INT NOT NULL DEFAULT 0,
    tournaments_participated INT NOT NULL DEFAULT 0,
    tournament_wins INT NOT NULL DEFAULT 0,
    tournament_runner_up INT NOT NULL DEFAULT 0,
    tournament_semi_final_reached INT NOT NULL DEFAULT 0,
    tournament_quarter_final_reached INT NOT NULL DEFAULT 0,
    last_match_at TIMESTAMP NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_player_club_stats_player
        FOREIGN KEY (player_id)
        REFERENCES player(id),
    CONSTRAINT fk_player_club_stats_club
        FOREIGN KEY (club_id)
        REFERENCES club(id),
    CONSTRAINT uq_player_club_stats_player_club
        UNIQUE (player_id, club_id)
);

CREATE INDEX idx_player_club_stats_player_id ON player_club_stats(player_id);
CREATE INDEX idx_player_club_stats_club_id ON player_club_stats(club_id);

CREATE TABLE tournament_summary (
    tournament_id BIGINT PRIMARY KEY,
    total_matches_played INT NOT NULL DEFAULT 0,
    total_points_scored INT NOT NULL DEFAULT 0,
    participant_count INT NOT NULL DEFAULT 0,
    champion_player_id BIGINT NULL,
    runner_up_player_id BIGINT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tournament_summary_tournament
        FOREIGN KEY (tournament_id)
        REFERENCES tournament_metadata(id),
    CONSTRAINT fk_tournament_summary_champion
        FOREIGN KEY (champion_player_id)
        REFERENCES player(id),
    CONSTRAINT fk_tournament_summary_runner_up
        FOREIGN KEY (runner_up_player_id)
        REFERENCES player(id)
);
