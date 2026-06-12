CREATE TABLE club_member (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    club_id BIGINT NOT NULL,
    player_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_club_member_club
        FOREIGN KEY (club_id)
        REFERENCES club(id),
    CONSTRAINT fk_club_member_player
        FOREIGN KEY (player_id)
        REFERENCES player(id),
    CONSTRAINT uq_club_member_club_player
        UNIQUE (club_id, player_id)
);

CREATE INDEX idx_club_member_club_id ON club_member(club_id);
CREATE INDEX idx_club_member_player_id ON club_member(player_id);
