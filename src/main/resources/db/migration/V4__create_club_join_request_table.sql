CREATE TABLE club_join_request (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    club_id BIGINT NOT NULL,
    player_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    reviewed_by BIGINT,
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_club_join_request_club
        FOREIGN KEY (club_id)
        REFERENCES club(id),
    CONSTRAINT fk_club_join_request_player
        FOREIGN KEY (player_id)
        REFERENCES player(id),
    CONSTRAINT fk_club_join_request_reviewed_by
        FOREIGN KEY (reviewed_by)
        REFERENCES player(id),
    CONSTRAINT uq_club_join_request_club_player
        UNIQUE (club_id, player_id)
);

CREATE INDEX idx_club_join_request_club_id ON club_join_request(club_id);
CREATE INDEX idx_club_join_request_player_id ON club_join_request(player_id);
CREATE INDEX idx_club_join_request_status ON club_join_request(status);
