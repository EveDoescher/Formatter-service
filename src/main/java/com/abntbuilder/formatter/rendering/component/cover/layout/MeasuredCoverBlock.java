package com.abntbuilder.formatter.rendering.component.cover.layout;

import com.abntbuilder.formatter.profile.model.StyleRule;

import java.util.List;
import java.util.Objects;

public record MeasuredCoverBlock(
        String blockId,
        StyleRule styleRule,
        List<String> lines
) {

    public MeasuredCoverBlock {
        requireNonBlank(blockId, "blockId");
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

    public int occupiedLines() {
        return lines.size();
    }

    public CoverTextLines toTextElement() {
        return new CoverTextLines(blockId, styleRule, lines);
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
