CREATE TABLE club (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    club_code VARCHAR(10) UNIQUE NOT NULL,
    club_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    owner_player_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_club_owner_player
        FOREIGN KEY (owner_player_id)
        REFERENCES player(id)
);

CREATE INDEX idx_club_owner_player_id ON club(owner_player_id);

CREATE SEQUENCE club_code_sequence START WITH 1 INCREMENT BY 1;
