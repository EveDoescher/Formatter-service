package com.abntbuilder.formatter.profile.model;

import java.math.BigDecimal;
import java.util.Objects;

public record StyleRule(
        String id,
        StyleType type,
        String fontFamily,
        BigDecimal fontSizePt,
        TextAlignment alignment,
        BigDecimal lineSpacing,
        BigDecimal firstLineIndentCm,
        BigDecimal leftIndentCm,
        BigDecimal rightIndentCm,
        BigDecimal spacingBeforePt,
        BigDecimal spacingAfterPt,
        boolean bold,
        boolean italic,
        boolean uppercase
) {
    public StyleRule {
        requireNonBlank(id, "id");
        Objects.requireNonNull(type, "type must not be null");
        requireNonBlank(fontFamily, "fontFamily");
        requirePositive(fontSizePt, "fontSizePt");
        Objects.requireNonNull(alignment, "alignment must not be null");
        requirePositive(lineSpacing, "lineSpacing");
        requireNonNegative(firstLineIndentCm, "firstLineIndentCm");
        requireNonNegative(leftIndentCm, "leftIndentCm");
        requireNonNegative(rightIndentCm, "rightIndentCm");
        requireNonNegative(spacingBeforePt, "spacingBeforePt");
        requireNonNegative(spacingAfterPt, "spacingAfterPt");
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