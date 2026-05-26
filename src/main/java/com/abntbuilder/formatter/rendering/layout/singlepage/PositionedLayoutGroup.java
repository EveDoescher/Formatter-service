package com.abntbuilder.formatter.rendering.layout.singlepage;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public record PositionedLayoutGroup(
        String id,
        BigDecimal yStartCm,
        BigDecimal heightCm,
        List<SinglePageLayoutTextLine> lines
) {
    public PositionedLayoutGroup {
        requireNonBlank(id, "id");
        requireNonNegative(yStartCm, "yStartCm");
        requirePositive(heightCm, "heightCm");
        Objects.requireNonNull(lines, "lines must not be null");

        if (lines.isEmpty()) {
            throw new IllegalArgumentException("lines must not be empty.");
        }

        lines = List.copyOf(lines);

        for (SinglePageLayoutTextLine line : lines) {
            Objects.requireNonNull(line, "lines must not contain null values.");
        }
    }

    public BigDecimal yEndCm() {
        return yStartCm.add(heightCm);
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }

    private static void requirePositive(BigDecimal value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");

        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than zero.");
        }
    }

    private static void requireNonNegative(BigDecimal value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");

        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(fieldName + " must not be negative.");
        }
    }
}