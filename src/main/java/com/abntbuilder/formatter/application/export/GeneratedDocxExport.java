package com.abntbuilder.formatter.application.export;

import java.time.Instant;
import java.util.Objects;

public record GeneratedDocxExport(
        String id,
        String fileName,
        byte[] bytes,
        Instant createdAt
) {
    public GeneratedDocxExport {
        requireNonBlank(id, "id");
        requireNonBlank(fileName, "fileName");
        Objects.requireNonNull(bytes, "bytes must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");

        if (bytes.length == 0) {
            throw new IllegalArgumentException("bytes must not be empty.");
        }

        bytes = bytes.clone();
    }

    @Override
    public byte[] bytes() {
        return bytes.clone();
    }

    public long sizeBytes() {
        return bytes.length;
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}