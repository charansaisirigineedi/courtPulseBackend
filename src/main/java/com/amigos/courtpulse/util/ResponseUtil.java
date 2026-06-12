package com.amigos.courtpulse.util;

import com.amigos.courtpulse.dto.common.ApiResponse;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public final class ResponseUtil {

    private ResponseUtil() {
    }

    public static <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return success(HttpStatus.OK, message, data);
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(String message, T data, URI location) {
        return ResponseEntity
                .created(location)
                .body(response(HttpStatus.CREATED, message, data, null, null));
    }

    public static <T> ResponseEntity<ApiResponse<T>> success(
            HttpStatus status,
            String message,
            T data
    ) {
        return ResponseEntity
                .status(status)
                .body(response(status, message, data, null, null));
    }

    public static ResponseEntity<ApiResponse<Void>> error(
            HttpStatus status,
            String message,
            String path
    ) {
        return error(status, message, path, null);
    }

    public static ResponseEntity<ApiResponse<Void>> error(
            HttpStatus status,
            String message,
            String path,
            Map<String, String> errors
    ) {
        return ResponseEntity
                .status(status)
                .body(ResponseUtil.<Void>response(status, message, null, errors, path));
    }

    private static <T> ApiResponse<T> response(
            HttpStatus status,
            String message,
            T data,
            Map<String, String> errors,
            String path
    ) {
        return new ApiResponse<>(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                data,
                errors,
                path
        );
    }
}
