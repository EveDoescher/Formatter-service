package com.abntbuilder.formatter.rendering.layout.singlepage;

import java.util.List;
import java.util.Objects;

public record SinglePageLayoutGroup(
        String id,
        List<SinglePageLayoutTextLine> lines
) {
    public SinglePageLayoutGroup {
        requireNonBlank(id, "id");
        Objects.requireNonNull(lines, "lines must not be null");

        if (lines.isEmpty()) {
            throw new IllegalArgumentException("lines must not be empty.");
        }

        lines = List.copyOf(lines);

        for (SinglePageLayoutTextLine line : lines) {
            Objects.requireNonNull(line, "lines must not contain null values.");
        }
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}