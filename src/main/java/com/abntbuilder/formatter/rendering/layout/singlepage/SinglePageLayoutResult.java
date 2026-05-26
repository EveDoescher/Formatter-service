package com.abntbuilder.formatter.rendering.layout.singlepage;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public record SinglePageLayoutResult(
        BigDecimal usableHeightCm,
        BigDecimal contentHeightCm,
        BigDecimal gapBetweenGroupsCm,
        List<PositionedLayoutGroup> groups
) {
    public SinglePageLayoutResult {
        requirePositive(usableHeightCm, "usableHeightCm");
        requirePositive(contentHeightCm, "contentHeightCm");
        requireNonNegative(gapBetweenGroupsCm, "gapBetweenGroupsCm");
        Objects.requireNonNull(groups, "groups must not be null");

        if (groups.isEmpty()) {
            throw new IllegalArgumentException("groups must not be empty.");
        }

        groups = List.copyOf(groups);

        for (PositionedLayoutGroup group : groups) {
            Objects.requireNonNull(group, "groups must not contain null values.");
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