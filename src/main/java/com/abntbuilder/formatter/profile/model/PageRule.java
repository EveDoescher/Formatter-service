package com.abntbuilder.formatter.profile.model;

import java.math.BigDecimal;
import java.util.Objects;

public record PageRule(
        BigDecimal widthCm,
        BigDecimal heightCm,
        BigDecimal marginTopCm,
        BigDecimal marginRightCm,
        BigDecimal marginBottomCm,
        BigDecimal marginLeftCm,
        PageOrientation orientation
) {
    public PageRule {
        requirePositive(widthCm, "widthCm");
        requirePositive(heightCm, "heightCm");
        requireNonNegative(marginTopCm, "marginTopCm");
        requireNonNegative(marginRightCm, "marginRightCm");
        requireNonNegative(marginBottomCm, "marginBottomCm");
        requireNonNegative(marginLeftCm, "marginLeftCm");
        Objects.requireNonNull(orientation, "orientation must not be null");

        BigDecimal horizontalMargins = marginLeftCm.add(marginRightCm);
        BigDecimal verticalMargins = marginTopCm.add(marginBottomCm);

        if (horizontalMargins.compareTo(widthCm) >= 0) {
            throw new IllegalArgumentException("Horizontal margins must be smaller than page width.");
        }

        if (verticalMargins.compareTo(heightCm) >= 0) {
            throw new IllegalArgumentException("Vertical margins must be smaller than page height.");
        }
    }

    public BigDecimal usableWidthCm() {
        return widthCm.subtract(marginLeftCm).subtract(marginRightCm);
    }

    public BigDecimal usableHeightCm() {
        return heightCm.subtract(marginTopCm).subtract(marginBottomCm);
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