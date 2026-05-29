package com.abntbuilder.formatter.rendering.layout.text;

import com.abntbuilder.formatter.profile.model.PageRule;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.shared.exception.TextMeasurementException;
import com.abntbuilder.formatter.shared.measurement.MeasurementConverter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class ConservativeTextMeasurer implements TextMeasurer {

    private static final BigDecimal REGULAR_CHARACTER_WIDTH_FACTOR = BigDecimal.valueOf(0.62);
    private static final BigDecimal BOLD_CHARACTER_WIDTH_FACTOR = BigDecimal.valueOf(0.64);
    private static final BigDecimal UPPERCASE_CHARACTER_WIDTH_FACTOR = BigDecimal.valueOf(0.66);
    private static final BigDecimal BOLD_UPPERCASE_CHARACTER_WIDTH_FACTOR = BigDecimal.valueOf(0.68);

    @Override
    public MeasuredText measure(
            String text,
            PageRule pageRule,
            StyleRule styleRule
    ) {
        if (text == null || text.isBlank()) {
            throw TextMeasurementException.blankText();
        }

        Objects.requireNonNull(pageRule, "pageRule must not be null");
        Objects.requireNonNull(styleRule, "styleRule must not be null");

        BigDecimal availableTextWidthPt = calculateAvailableTextWidthPt(pageRule, styleRule);
        List<String> visualLines = new ArrayList<>();

        String resolvedText = resolveLayoutText(text, styleRule);
        String[] explicitLines = resolvedText.strip().split("\\R");

        for (String explicitLine : explicitLines) {
            visualLines.addAll(breakSingleLine(explicitLine, availableTextWidthPt, styleRule));
        }

        return new MeasuredText(visualLines);
    }

    private static List<String> breakSingleLine(
            String text,
            BigDecimal availableTextWidthPt,
            StyleRule styleRule
    ) {
        String normalized = text.trim().replaceAll("\\s+", " ");

        if (normalized.isBlank()) {
            throw TextMeasurementException.blankText();
        }

        List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();

        String[] words = normalized.split(" ");

        for (String word : words) {
            if (estimatedTextWidthPt(word, styleRule).compareTo(availableTextWidthPt) > 0) {
                throw TextMeasurementException.wordExceedsAvailableWidth();
            }

            String candidate = currentLine.isEmpty()
                    ? word
                    : currentLine + " " + word;

            if (estimatedTextWidthPt(candidate, styleRule).compareTo(availableTextWidthPt) <= 0) {
                currentLine = new StringBuilder(candidate);
            } else {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            }
        }

        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }

        return List.copyOf(lines);
    }

    private static BigDecimal calculateAvailableTextWidthPt(
            PageRule pageRule,
            StyleRule styleRule
    ) {
        BigDecimal usableWidthPt = MeasurementConverter.centimetersToPoints(pageRule.usableWidthCm());
        BigDecimal leftIndentPt = MeasurementConverter.centimetersToPoints(styleRule.leftIndentCm());
        BigDecimal rightIndentPt = MeasurementConverter.centimetersToPoints(styleRule.rightIndentCm());

        BigDecimal availableWidthPt = usableWidthPt
                .subtract(leftIndentPt)
                .subtract(rightIndentPt);

        if (availableWidthPt.compareTo(BigDecimal.ZERO) <= 0) {
            throw TextMeasurementException.unavailableTextWidth();
        }

        return availableWidthPt;
    }

    private static BigDecimal estimatedTextWidthPt(String text, StyleRule styleRule) {
        return BigDecimal.valueOf(text.length())
                .multiply(styleRule.fontSizePt())
                .multiply(averageCharacterWidthFactor(styleRule));
    }

    private static BigDecimal averageCharacterWidthFactor(StyleRule styleRule) {
        if (styleRule.bold() && styleRule.uppercase()) {
            return BOLD_UPPERCASE_CHARACTER_WIDTH_FACTOR;
        }

        if (styleRule.uppercase()) {
            return UPPERCASE_CHARACTER_WIDTH_FACTOR;
        }

        if (styleRule.bold()) {
            return BOLD_CHARACTER_WIDTH_FACTOR;
        }

        return REGULAR_CHARACTER_WIDTH_FACTOR;
    }

    private static String resolveLayoutText(String text, StyleRule styleRule) {
        if (styleRule.uppercase()) {
            return text.toUpperCase(Locale.ROOT);
        }

        return text;
    }
}
