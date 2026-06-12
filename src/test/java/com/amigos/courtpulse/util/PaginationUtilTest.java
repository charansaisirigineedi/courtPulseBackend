package com.amigos.courtpulse.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

class PaginationUtilTest {

    @Test
    void toOffsetPageableBuildsPageFromLimitAndOffset() {
        Pageable pageable = PaginationUtil.toOffsetPageable(20, 20, 20, 50);

        assertEquals(20, pageable.getPageSize());
        assertEquals(1, pageable.getPageNumber());
        assertEquals(20, pageable.getOffset());
    }

    @Test
    void toOffsetPageableClampsLimitToMax() {
        Pageable pageable = PaginationUtil.toOffsetPageable(100, 0, 20, 50);

        assertEquals(50, pageable.getPageSize());
        assertEquals(0, pageable.getPageNumber());
    }

    @Test
    void normalizeClampsPageSizeForDirectPageableInput() {
        Pageable pageable = PaginationUtil.normalize(PageRequest.of(0, 100), 20, 50);

        assertEquals(50, pageable.getPageSize());
        assertEquals(0, pageable.getPageNumber());
    }
}
