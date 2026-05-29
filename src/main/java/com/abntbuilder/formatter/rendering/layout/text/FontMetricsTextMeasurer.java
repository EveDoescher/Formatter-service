package com.abntbuilder.formatter.rendering.layout.text;

import com.abntbuilder.formatter.profile.model.PageRule;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.shared.exception.TextMeasurementException;
import com.abntbuilder.formatter.shared.measurement.MeasurementConverter;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class FontMetricsTextMeasurer implements TextMeasurer {

    private static final FontRenderContext FONT_RENDER_CONTEXT = new FontRenderContext(
            new AffineTransform(),
            true,
            true
    );
    private static final Set<String> AVAILABLE_FONT_FAMILIES = Arrays.stream(
                    GraphicsEnvironment
                            .getLocalGraphicsEnvironment()
                            .getAvailableFontFamilyNames(Locale.ROOT)
            )
            .map(fontFamily -> fontFamily.toLowerCase(Locale.ROOT))
            .collect(Collectors.toUnmodifiableSet());

    private final MissingFontPolicy missingFontPolicy;

    public FontMetricsTextMeasurer() {
        this(MissingFontPolicy.FAIL);
    }

    public FontMetricsTextMeasurer(MissingFontPolicy missingFontPolicy) {
        this.missingFontPolicy = Objects.requireNonNull(
                missingFontPolicy,
                "missingFontPolicy must not be null"
        );
    }

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

        double availableTextWidthPt = calculateAvailableTextWidthPt(pageRule, styleRule);
        Font font = createFont(styleRule, missingFontPolicy);
        List<String> visualLines = new ArrayList<>();

        String resolvedText = resolveLayoutText(text, styleRule);
        String[] explicitLines = resolvedText.strip().split("\\R");

        for (String explicitLine : explicitLines) {
            visualLines.addAll(breakSingleLine(explicitLine, availableTextWidthPt, font));
        }

        return new MeasuredText(visualLines);
    }

    private static List<String> breakSingleLine(
            String text,
            double availableTextWidthPt,
            Font font
    ) {
        String normalized = text.trim().replaceAll("\\s+", " ");

        if (normalized.isBlank()) {
            throw TextMeasurementException.blankText();
        }

        List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();

        String[] words = normalized.split(" ");

        for (String word : words) {
            if (measureTextWidthPt(word, font) > availableTextWidthPt) {
                throw TextMeasurementException.wordExceedsAvailableWidth();
            }

            String candidate = currentLine.isEmpty()
                    ? word
                    : currentLine + " " + word;

            if (measureTextWidthPt(candidate, font) <= availableTextWidthPt) {
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

    private static double calculateAvailableTextWidthPt(PageRule pageRule, StyleRule styleRule) {
        BigDecimal usableWidthPt = MeasurementConverter.centimetersToPoints(pageRule.usableWidthCm());
        BigDecimal leftIndentPt = MeasurementConverter.centimetersToPoints(styleRule.leftIndentCm());
        BigDecimal rightIndentPt = MeasurementConverter.centimetersToPoints(styleRule.rightIndentCm());

        BigDecimal availableWidthPt = usableWidthPt
                .subtract(leftIndentPt)
                .subtract(rightIndentPt);

        if (availableWidthPt.compareTo(BigDecimal.ZERO) <= 0) {
            throw TextMeasurementException.unavailableTextWidth();
        }

        return availableWidthPt.doubleValue();
    }

    private static Font createFont(
            StyleRule styleRule,
            MissingFontPolicy missingFontPolicy
    ) {
        if (missingFontPolicy == MissingFontPolicy.FAIL && !isFontAvailable(styleRule.fontFamily())) {
            throw TextMeasurementException.unavailableFontFamily(styleRule.fontFamily());
        }

        int style = Font.PLAIN;

        if (styleRule.bold()) {
            style |= Font.BOLD;
        }

        if (styleRule.italic()) {
            style |= Font.ITALIC;
        }

        return new Font(styleRule.fontFamily(), style, 1)
                .deriveFont(styleRule.fontSizePt().floatValue());
    }

    private static boolean isFontAvailable(String fontFamily) {
        return fontFamily != null
                && AVAILABLE_FONT_FAMILIES.contains(fontFamily.toLowerCase(Locale.ROOT));
    }

    private static double measureTextWidthPt(String text, Font font) {
        return font.getStringBounds(text, FONT_RENDER_CONTEXT).getWidth();
    }

    private static String resolveLayoutText(String text, StyleRule styleRule) {
        if (styleRule.uppercase()) {
            return text.toUpperCase(Locale.ROOT);
        }

        return text;
    }
}
