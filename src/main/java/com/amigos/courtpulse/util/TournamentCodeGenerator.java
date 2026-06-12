package com.amigos.courtpulse.util;

import com.amigos.courtpulse.repository.TournamentMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TournamentCodeGenerator {

    private static final String PREFIX = "T";
    private static final int CODE_PART_LENGTH = 6;
    private static final long MAX_SEQUENCE_VALUE = 999_999L;
    private static final String NEXT_SEQUENCE_VALUE_SQL = "SELECT nextval('tournament_code_sequence')";

    private final TournamentMetadataRepository tournamentMetadataRepository;
    private final JdbcTemplate jdbcTemplate;

    public String generateUniqueCode() {
        while (true) {
            String tournamentCode = nextCode();
            if (!tournamentMetadataRepository.existsByTournamentCode(tournamentCode)) {
                return tournamentCode;
            }
        }
    }

    private String nextCode() {
        Long sequenceValue = jdbcTemplate.queryForObject(NEXT_SEQUENCE_VALUE_SQL, Long.class);
        if (ObjectUtil.isNull(sequenceValue) || sequenceValue > MAX_SEQUENCE_VALUE) {
            throw new IllegalStateException("Tournament code sequence is exhausted");
        }

        return PREFIX + ("%0" + CODE_PART_LENGTH + "d").formatted(sequenceValue);
    }
}
