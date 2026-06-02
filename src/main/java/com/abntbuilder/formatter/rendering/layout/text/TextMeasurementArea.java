package com.abntbuilder.formatter.rendering.layout.text;

import com.abntbuilder.formatter.profile.model.PageRule;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.shared.exception.TextMeasurementException;

import java.math.BigDecimal;
import java.util.Objects;

public record TextMeasurementArea(
        BigDecimal availableWidthCm,
        BigDecimal leftIndentCm,
        BigDecimal rightIndentCm
) {

    public TextMeasurementArea {
        Objects.requireNonNull(availableWidthCm, "availableWidthCm must not be null");
        Objects.requireNonNull(leftIndentCm, "leftIndentCm must not be null");
        Objects.requireNonNull(rightIndentCm, "rightIndentCm must not be null");

        if (availableWidthCm.compareTo(BigDecimal.ZERO) <= 0) {
            throw TextMeasurementException.unavailableTextWidth();
        }

        if (leftIndentCm.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("leftIndentCm must not be negative.");
        }

        if (rightIndentCm.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("rightIndentCm must not be negative.");
        }
    }

    public static TextMeasurementArea fullContentWidth(PageRule pageRule) {
        Objects.requireNonNull(pageRule, "pageRule must not be null");

        return new TextMeasurementArea(
                pageRule.usableWidthCm(),
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
    }

    public static TextMeasurementArea fromStyle(PageRule pageRule, StyleRule styleRule) {
        Objects.requireNonNull(pageRule, "pageRule must not be null");
        Objects.requireNonNull(styleRule, "styleRule must not be null");

        BigDecimal availableWidthCm = pageRule.usableWidthCm()
                .subtract(styleRule.leftIndentCm())
                .subtract(styleRule.rightIndentCm());

        return new TextMeasurementArea(
                availableWidthCm,
                styleRule.leftIndentCm(),
                styleRule.rightIndentCm()
        );
    }
}
