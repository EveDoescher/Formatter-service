package com.abntbuilder.formatter.rendering.singlepage;

import com.abntbuilder.formatter.engine.model.output.ParagraphLayoutOverride;
import com.abntbuilder.formatter.engine.model.profile.StyleRule;
import com.abntbuilder.formatter.rendering.text.TextMeasurementArea;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record SinglePageLayoutItem(
        String id,
        StyleRule styleRule,
        String paragraphText,
        List<String> visualLines,
        Optional<TextMeasurementArea> measurementArea,
        ParagraphLayoutOverride layoutOverride,
        int blankLinesAfter
) {

    public SinglePageLayoutItem(
            String id,
            StyleRule styleRule,
            List<String> visualLines
    ) {
        this(
                id,
                styleRule,
                String.join(" ", visualLines),
                visualLines,
                Optional.empty(),
                ParagraphLayoutOverride.none(),
                0
        );
    }

    public SinglePageLayoutItem(
            String id,
            StyleRule styleRule,
            String paragraphText,
            List<String> visualLines
    ) {
        this(
                id,
                styleRule,
                paragraphText,
                visualLines,
                Optional.empty(),
                ParagraphLayoutOverride.none(),
                0
        );
    }

    public SinglePageLayoutItem(
            String id,
            StyleRule styleRule,
            String paragraphText,
            List<String> visualLines,
            Optional<TextMeasurementArea> measurementArea,
            ParagraphLayoutOverride layoutOverride
    ) {
        this(
                id,
                styleRule,
                paragraphText,
                visualLines,
                measurementArea,
                layoutOverride,
                0
        );
    }

    public SinglePageLayoutItem {
        requireNonBlank(id, "id");
        Objects.requireNonNull(styleRule, "styleRule must not be null");
        requireNonBlank(paragraphText, "paragraphText");
        Objects.requireNonNull(visualLines, "visualLines must not be null");
        Objects.requireNonNull(measurementArea, "measurementArea must not be null");
        Objects.requireNonNull(layoutOverride, "layoutOverride must not be null");

        if (blankLinesAfter < 0) {
            throw new IllegalArgumentException("blankLinesAfter must not be negative.");
        }

        if (visualLines.isEmpty()) {
            throw new IllegalArgumentException("visualLines must not be empty.");
        }

        visualLines = List.copyOf(visualLines);

        for (String visualLine : visualLines) {
            requireNonBlank(visualLine, "visualLines item");
        }
    }

    public int lineCount() {
        return visualLines.size() + blankLinesAfter;
    }

    public int visualLineCount() {
        return visualLines.size();
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
