package com.abntbuilder.formatter.rendering.layout.text;

import java.util.List;
import java.util.Objects;

public record MeasuredText(
        List<String> visualLines
) {

    public MeasuredText {
        Objects.requireNonNull(visualLines, "visualLines must not be null");

        if (visualLines.isEmpty()) {
            throw new IllegalArgumentException("visualLines must not be empty.");
        }

        visualLines = List.copyOf(visualLines);

        for (String line : visualLines) {
            if (line == null || line.isBlank()) {
                throw new IllegalArgumentException("visualLines must not contain blank values.");
            }
        }
    }

    public int lineCount() {
        return visualLines.size();
    }
}
