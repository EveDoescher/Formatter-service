package com.abntbuilder.formatter.rendering.layout.singlepage;

import com.abntbuilder.formatter.profile.model.StyleRule;

import java.util.Objects;

public record SinglePageLayoutTextLine(
        String text,
        StyleRule styleRule
) {
    public SinglePageLayoutTextLine {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text must not be blank.");
        }

        Objects.requireNonNull(styleRule, "styleRule must not be null");
    }
}