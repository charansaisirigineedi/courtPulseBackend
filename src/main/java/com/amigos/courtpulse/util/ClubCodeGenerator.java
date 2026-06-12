package com.amigos.courtpulse.util;

import com.amigos.courtpulse.repository.ClubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClubCodeGenerator {

    private static final String PREFIX = "CLB";
    private static final int CODE_PART_LENGTH = 3;
    private static final long MAX_SEQUENCE_VALUE = 999L;
    private static final String NEXT_SEQUENCE_VALUE_SQL = "SELECT nextval('club_code_sequence')";

    private final ClubRepository clubRepository;
    private final JdbcTemplate jdbcTemplate;

    public String generateUniqueCode() {
        while (true) {
            String clubCode = nextCode();
            if (!clubRepository.existsByClubCode(clubCode)) {
                return clubCode;
            }
        }
    }

    private String nextCode() {
        Long sequenceValue = jdbcTemplate.queryForObject(NEXT_SEQUENCE_VALUE_SQL, Long.class);
        if (ObjectUtil.isNull(sequenceValue) || sequenceValue > MAX_SEQUENCE_VALUE) {
            throw new IllegalStateException("Club code sequence is exhausted");
        }

        return PREFIX + "%03d".formatted(sequenceValue);
    }
}
