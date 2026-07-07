package com.abntbuilder.formatter.rendering.singlepage;

import com.abntbuilder.formatter.engine.model.output.ParagraphLayoutOverride;
import com.abntbuilder.formatter.engine.model.profile.StyleRule;
import com.abntbuilder.formatter.rendering.text.TextMeasurementArea;
import com.abntbuilder.formatter.shared.measurement.MeasurementConverter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record SinglePageTextLines(
        String groupId,
        String itemId,
        StyleRule styleRule,
        String paragraphText,
        List<String> lines,
        Optional<TextMeasurementArea> measurementArea,
        ParagraphLayoutOverride layoutOverride,
        int lineHeightTwips
) implements SinglePageLayoutElement {

    public SinglePageTextLines(
            String groupId,
            String itemId,
            StyleRule styleRule,
            List<String> lines
    ) {
        this(
                groupId,
                itemId,
                styleRule,
                String.join(" ", lines),
                lines,
                Optional.empty(),
                ParagraphLayoutOverride.none(),
                MeasurementConverter.pointsToTwips(styleRule.fontSizePt().multiply(styleRule.lineSpacing()))
        );
    }

    public SinglePageTextLines(
            String groupId,
            String itemId,
            StyleRule styleRule,
            String paragraphText,
            List<String> lines
    ) {
        this(
                groupId,
                itemId,
                styleRule,
                paragraphText,
                lines,
                Optional.empty(),
                ParagraphLayoutOverride.none(),
                MeasurementConverter.pointsToTwips(styleRule.fontSizePt().multiply(styleRule.lineSpacing()))
        );
    }

    public SinglePageTextLines {
        requireNonBlank(groupId, "groupId");
        requireNonBlank(itemId, "itemId");
        Objects.requireNonNull(styleRule, "styleRule must not be null");
        requireNonBlank(paragraphText, "paragraphText");
        Objects.requireNonNull(lines, "lines must not be null");
        Objects.requireNonNull(measurementArea, "measurementArea must not be null");
        Objects.requireNonNull(layoutOverride, "layoutOverride must not be null");

        if (lineHeightTwips <= 0) {
            throw new IllegalArgumentException("lineHeightTwips must be greater than zero.");
        }

        if (lines.isEmpty()) {
            throw new IllegalArgumentException("lines must not be empty.");
        }

        lines = List.copyOf(lines);

        for (String line : lines) {
            requireNonBlank(line, "lines item");
        }
    }

    @Override
    public int lineCount() {
        return lines.size();
    }

    @Override
    public int heightTwips() {
        return lineCount() * lineHeightTwips;
    }

    public BigDecimal exactLineHeightPt() {
        return MeasurementConverter.twipsToPoints(lineHeightTwips);
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
