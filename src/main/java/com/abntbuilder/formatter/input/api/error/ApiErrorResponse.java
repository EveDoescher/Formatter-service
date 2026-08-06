package com.abntbuilder.formatter.input.api.error;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
        String code,
        String message,
        String traceId,
        String path,
        String timestamp,
        List<ValidationError> errors
) {
    public static ApiErrorResponse of(String code, String message, String traceId, String path) {
        return new ApiErrorResponse(code, message, traceId, path, Instant.now().toString(), List.of());
    }

    public static ApiErrorResponse of(String code, String message, String traceId, String path, List<ValidationError> errors) {
        return new ApiErrorResponse(code, message, traceId, path, Instant.now().toString(), List.copyOf(errors));
    }

    public record ValidationError(
            String field,
            String message
    ) {
    }
}
