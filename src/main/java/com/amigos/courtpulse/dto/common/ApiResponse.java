package com.amigos.courtpulse.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        LocalDateTime timestamp,
        int statusCode,
        String status,
        String message,
        T data,
        Map<String, String> errors,
        String path
) {
}
