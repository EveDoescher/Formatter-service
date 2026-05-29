package com.abntbuilder.formatter.rendering.layout.singlepage;

import com.abntbuilder.formatter.profile.model.StyleRule;

import java.util.List;
import java.util.Objects;

public record SinglePageLayoutItem(
        String id,
        StyleRule styleRule,
        List<String> visualLines
) {

    public SinglePageLayoutItem {
        requireNonBlank(id, "id");
        Objects.requireNonNull(styleRule, "styleRule must not be null");
        Objects.requireNonNull(visualLines, "visualLines must not be null");

        if (visualLines.isEmpty()) {
            throw new IllegalArgumentException("visualLines must not be empty.");
        }

        visualLines = List.copyOf(visualLines);

        for (String visualLine : visualLines) {
            requireNonBlank(visualLine, "visualLines item");
        }
    }

    public int lineCount() {
        return visualLines.size();
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
