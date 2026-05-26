package com.abntbuilder.formatter.shared.measurement;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class MeasurementConverter {

    private static final BigDecimal TWIPS_PER_INCH = BigDecimal.valueOf(1440);
    private static final BigDecimal CENTIMETERS_PER_INCH = BigDecimal.valueOf(2.54);
    private static final BigDecimal POINTS_PER_INCH = BigDecimal.valueOf(72);
    private static final BigDecimal TWIPS_PER_POINT = BigDecimal.valueOf(20);
    private static final BigDecimal HALF_POINTS_PER_POINT = BigDecimal.valueOf(2);
    private static final BigDecimal DOCX_AUTO_LINE_SPACING_UNIT = BigDecimal.valueOf(240);

    private MeasurementConverter() {
    }

    public static int centimetersToTwips(BigDecimal centimeters) {
        Objects.requireNonNull(centimeters, "centimeters must not be null");

        return centimeters
                .multiply(TWIPS_PER_INCH)
                .divide(CENTIMETERS_PER_INCH, 0, RoundingMode.HALF_UP)
                .intValueExact();
    }

    public static BigDecimal centimetersToPoints(BigDecimal centimeters) {
        Objects.requireNonNull(centimeters, "centimeters must not be null");

        return centimeters
                .multiply(POINTS_PER_INCH)
                .divide(CENTIMETERS_PER_INCH, 6, RoundingMode.HALF_UP);
    }

    public static BigDecimal pointsToCentimeters(BigDecimal points) {
        Objects.requireNonNull(points, "points must not be null");

        return points
                .multiply(CENTIMETERS_PER_INCH)
                .divide(POINTS_PER_INCH, 6, RoundingMode.HALF_UP);
    }

    public static int pointsToTwips(BigDecimal points) {
        Objects.requireNonNull(points, "points must not be null");

        return points
                .multiply(TWIPS_PER_POINT)
                .setScale(0, RoundingMode.HALF_UP)
                .intValueExact();
    }

    public static BigDecimal twipsToPoints(int twips) {
        if (twips <= 0) {
            throw new IllegalArgumentException("twips must be greater than zero.");
        }

        return BigDecimal.valueOf(twips)
                .divide(TWIPS_PER_POINT, 6, RoundingMode.HALF_UP);
    }

    public static int pointsToHalfPoints(BigDecimal points) {
        Objects.requireNonNull(points, "points must not be null");

        return points
                .multiply(HALF_POINTS_PER_POINT)
                .setScale(0, RoundingMode.HALF_UP)
                .intValueExact();
    }

    public static int lineSpacingMultiplierToDocxLineValue(BigDecimal lineSpacing) {
        Objects.requireNonNull(lineSpacing, "lineSpacing must not be null");

        if (lineSpacing.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("lineSpacing must be greater than zero.");
        }

        return lineSpacing
                .multiply(DOCX_AUTO_LINE_SPACING_UNIT)
                .setScale(0, RoundingMode.HALF_UP)
                .intValueExact();
    }
}