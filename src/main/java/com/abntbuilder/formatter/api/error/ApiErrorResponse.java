package com.abntbuilder.formatter.api.error;

import java.util.List;

public record ApiErrorResponse(
        String message,
        List<ValidationError> errors
) {
    public static ApiErrorResponse of(String message) {
        return new ApiErrorResponse(message, List.of());
    }

    public static ApiErrorResponse of(String message, List<ValidationError> errors) {
        return new ApiErrorResponse(message, List.copyOf(errors));
    }

    public record ValidationError(
            String field,
            String message
    ) {
    }
}