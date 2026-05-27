package com.abntbuilder.formatter.rendering.layout.singlepage;

import com.abntbuilder.formatter.profile.model.PageRule;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.shared.measurement.MeasurementConverter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class SinglePageTextLineBreaker {

    private static final BigDecimal REGULAR_CHARACTER_WIDTH_FACTOR = BigDecimal.valueOf(0.62);
    private static final BigDecimal BOLD_CHARACTER_WIDTH_FACTOR = BigDecimal.valueOf(0.64);
    private static final BigDecimal UPPERCASE_CHARACTER_WIDTH_FACTOR = BigDecimal.valueOf(0.66);
    private static final BigDecimal BOLD_UPPERCASE_CHARACTER_WIDTH_FACTOR = BigDecimal.valueOf(0.68);

    public List<String> breakText(String text, int maxCharactersPerLine) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text must not be blank.");
        }

        if (maxCharactersPerLine <= 0) {
            throw new IllegalArgumentException("maxCharactersPerLine must be greater than zero.");
        }

        List<String> result = new ArrayList<>();

        String[] explicitLines = text.strip().split("\\R");

        for (String explicitLine : explicitLines) {
            result.addAll(breakSingleLine(explicitLine, maxCharactersPerLine));
        }

        return List.copyOf(result);
    }

    public List<String> breakText(
            String text,
            int configuredMaxCharactersPerLine,
            PageRule pageRule,
            StyleRule styleRule
    ) {
        Objects.requireNonNull(pageRule, "pageRule must not be null");
        Objects.requireNonNull(styleRule, "styleRule must not be null");

        int conservativeMaxCharactersPerLine = conservativeMaxCharactersPerLine(
                configuredMaxCharactersPerLine,
                pageRule,
                styleRule
        );

        return breakText(text, conservativeMaxCharactersPerLine);
    }

    private static int conservativeMaxCharactersPerLine(
            int configuredMaxCharactersPerLine,
            PageRule pageRule,
            StyleRule styleRule
    ) {
        if (configuredMaxCharactersPerLine <= 0) {
            throw new IllegalArgumentException("configuredMaxCharactersPerLine must be greater than zero.");
        }

        BigDecimal usableWidthPt = MeasurementConverter.centimetersToPoints(pageRule.usableWidthCm());
        BigDecimal leftIndentPt = MeasurementConverter.centimetersToPoints(styleRule.leftIndentCm());
        BigDecimal rightIndentPt = MeasurementConverter.centimetersToPoints(styleRule.rightIndentCm());

        BigDecimal textWidthPt = usableWidthPt
                .subtract(leftIndentPt)
                .subtract(rightIndentPt);

        if (textWidthPt.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("single-page text width must be greater than zero.");
        }

        BigDecimal averageCharacterWidthPt = styleRule.fontSizePt()
                .multiply(averageCharacterWidthFactor(styleRule));

        int measuredMaxCharactersPerLine = textWidthPt
                .divide(averageCharacterWidthPt, 0, RoundingMode.FLOOR)
                .intValueExact();

        if (measuredMaxCharactersPerLine <= 0) {
            throw new IllegalArgumentException("single-page text width is too small for the selected style.");
        }

        return Math.min(
                configuredMaxCharactersPerLine,
                measuredMaxCharactersPerLine
        );
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

    private static List<String> breakSingleLine(String text, int maxCharactersPerLine) {
        Objects.requireNonNull(text, "text must not be null");

        String normalized = text.trim().replaceAll("\\s+", " ");

        if (normalized.isBlank()) {
            throw new IllegalArgumentException("text must not be blank.");
        }

        List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();

        String[] words = normalized.split(" ");

        for (String word : words) {
            if (word.length() > maxCharactersPerLine) {
                throw new IllegalArgumentException("word length exceeds maxCharactersPerLine.");
            }

            if (currentLine.isEmpty()) {
                currentLine.append(word);
                continue;
            }

            int candidateLength = currentLine.length() + 1 + word.length();

            if (candidateLength <= maxCharactersPerLine) {
                currentLine.append(' ').append(word);
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
}
