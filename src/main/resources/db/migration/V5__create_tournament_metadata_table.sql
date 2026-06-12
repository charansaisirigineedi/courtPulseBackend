CREATE SEQUENCE tournament_code_sequence START WITH 1 INCREMENT BY 1;

CREATE TABLE tournament_metadata (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tournament_code VARCHAR(10) NOT NULL UNIQUE,
    club_id BIGINT NOT NULL,
    tournament_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    tournament_type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,
    tournament_date DATE NOT NULL,
    registration_start_at TIMESTAMP,
    registration_end_at TIMESTAMP,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tournament_metadata_club
        FOREIGN KEY (club_id)
        REFERENCES club(id),
    CONSTRAINT fk_tournament_metadata_created_by
        FOREIGN KEY (created_by)
        REFERENCES player(id)
);

CREATE UNIQUE INDEX idx_tournament_metadata_tournament_code
    ON tournament_metadata(tournament_code);
CREATE INDEX idx_tournament_metadata_club_id
    ON tournament_metadata(club_id);
CREATE INDEX idx_tournament_metadata_tournament_date
    ON tournament_metadata(tournament_date);
CREATE INDEX idx_tournament_metadata_status
    ON tournament_metadata(status);
CREATE INDEX idx_tournament_metadata_created_by
    ON tournament_metadata(created_by);
