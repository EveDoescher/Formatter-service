package com.abntbuilder.formatter.rendering.component.cover.layout;

import com.abntbuilder.formatter.profile.model.StyleRule;

import java.util.Objects;

public record CoverSpacerLines(
        String gapId,
        int lineCount,
        StyleRule styleRule
) implements CoverLayoutElement {

    public CoverSpacerLines {
        requireNonBlank(gapId, "gapId");

        if (lineCount < 0) {
            throw new IllegalArgumentException("lineCount must not be negative.");
        }

        Objects.requireNonNull(styleRule, "styleRule must not be null");
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
