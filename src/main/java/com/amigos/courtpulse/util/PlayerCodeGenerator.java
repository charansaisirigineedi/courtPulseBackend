package com.amigos.courtpulse.util;

import com.amigos.courtpulse.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlayerCodeGenerator {

    private static final String PREFIX = "CP";
    private static final String ALLOWED_CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int CODE_PART_LENGTH = 4;
    private static final long MAX_SEQUENCE_VALUE = 1_679_615L;
    private static final String NEXT_SEQUENCE_VALUE_SQL = "SELECT nextval('player_code_sequence')";

    private final PlayerRepository playerRepository;
    private final JdbcTemplate jdbcTemplate;

    public String generateUniqueCode() {
        while (true) {
            String playerCode = nextCode();
            if (!playerRepository.existsByPlayerCode(playerCode)) {
                return playerCode;
            }
        }
    }

    private String nextCode() {
        Long sequenceValue = jdbcTemplate.queryForObject(NEXT_SEQUENCE_VALUE_SQL, Long.class);
        if (ObjectUtil.isNull(sequenceValue) || sequenceValue > MAX_SEQUENCE_VALUE) {
            throw new IllegalStateException("Player code sequence is exhausted");
        }

        return PREFIX + toBase36(sequenceValue);
    }

    private String toBase36(long value) {
        StringBuilder encoded = new StringBuilder();
        long currentValue = value;

        do {
            int remainder = (int) (currentValue % ALLOWED_CHARACTERS.length());
            encoded.append(ALLOWED_CHARACTERS.charAt(remainder));
            currentValue = currentValue / ALLOWED_CHARACTERS.length();
        } while (currentValue > 0);

        String codePart = encoded.reverse().toString();
        return "0".repeat(Math.max(0, CODE_PART_LENGTH - codePart.length())) + codePart;
    }
}
