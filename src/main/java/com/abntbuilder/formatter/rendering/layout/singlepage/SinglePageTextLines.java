package com.abntbuilder.formatter.rendering.layout.singlepage;

import com.abntbuilder.formatter.profile.model.StyleRule;

import java.util.List;
import java.util.Objects;

public record SinglePageTextLines(
        String groupId,
        String itemId,
        StyleRule styleRule,
        List<String> lines
) implements SinglePageLayoutElement {

    public SinglePageTextLines {
        requireNonBlank(groupId, "groupId");
        requireNonBlank(itemId, "itemId");
        Objects.requireNonNull(styleRule, "styleRule must not be null");
        Objects.requireNonNull(lines, "lines must not be null");

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

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
