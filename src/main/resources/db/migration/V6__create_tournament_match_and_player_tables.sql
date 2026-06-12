CREATE TABLE tournament_match (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tournament_id BIGINT NOT NULL,
    round_name VARCHAR(30) NOT NULL,
    match_no INT NOT NULL,
    score_a INT NOT NULL,
    score_b INT NOT NULL,
    winner_side VARCHAR(10) NOT NULL,
    completed_at TIMESTAMP NOT NULL,
    analytics_processed_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tournament_match_tournament
        FOREIGN KEY (tournament_id)
        REFERENCES tournament_metadata(id)
);

CREATE INDEX idx_tournament_match_tournament_id ON tournament_match(tournament_id);
CREATE INDEX idx_tournament_match_round_name ON tournament_match(round_name);
CREATE INDEX idx_tournament_match_completed_at ON tournament_match(completed_at);
CREATE INDEX idx_tournament_match_analytics_processed_at ON tournament_match(analytics_processed_at);
CREATE INDEX idx_tournament_match_tournament_round ON tournament_match(tournament_id, round_name);

CREATE TABLE tournament_match_player (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tournament_match_id BIGINT NOT NULL,
    player_id BIGINT NOT NULL,
    side VARCHAR(10) NOT NULL,
    is_winner BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_match_player_match
        FOREIGN KEY (tournament_match_id)
        REFERENCES tournament_match(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_match_player_player
        FOREIGN KEY (player_id)
        REFERENCES player(id),
    CONSTRAINT uq_tournament_match_player UNIQUE(tournament_match_id, player_id)
);

CREATE INDEX idx_tournament_match_player_player_id ON tournament_match_player(player_id);
CREATE INDEX idx_tournament_match_player_match_id ON tournament_match_player(tournament_match_id);
CREATE INDEX idx_tournament_match_player_player_winner ON tournament_match_player(player_id, is_winner);
CREATE INDEX idx_tournament_match_player_match_side ON tournament_match_player(tournament_match_id, side);

CREATE INDEX idx_tournament_metadata_club_date ON tournament_metadata(club_id, tournament_date);
