package com.amigos.courtpulse.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public final class PaginationUtil {

    private PaginationUtil() {
    }

    public static Pageable toOffsetPageable(int limit, int offset, int defaultLimit, int maxLimit) {
        int pageSize = clampLimit(limit, defaultLimit, maxLimit);
        int pageNumber = Math.max(offset, 0) / pageSize;
        return PageRequest.of(pageNumber, pageSize);
    }

    public static Pageable normalize(Pageable pageable, int defaultLimit, int maxLimit) {
        int pageSize = clampLimit(pageable.getPageSize(), defaultLimit, maxLimit);
        return PageRequest.of(pageNumber(pageable, pageSize), pageSize, pageable.getSort());
    }

    private static int pageNumber(Pageable pageable, int pageSize) {
        if (pageable.getOffset() > 0 && pageable.getPageSize() != pageSize) {
            return (int) (pageable.getOffset() / pageSize);
        }
        return pageable.getPageNumber();
    }

    private static int clampLimit(int requested, int defaultLimit, int maxLimit) {
        if (requested <= 0) {
            return defaultLimit;
        }
        return Math.min(requested, maxLimit);
    }
}
