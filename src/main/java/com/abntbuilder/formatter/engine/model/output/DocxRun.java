package com.abntbuilder.formatter.engine.model.output;

import com.abntbuilder.formatter.engine.model.content.bodycontent.InlineFormatting;
import com.abntbuilder.formatter.engine.model.profile.StyleRule;

import java.util.Objects;

public record DocxRun(
        String text,
        StyleRule baseStyle,
        InlineFormatting formatting
) {

    public DocxRun {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text must not be blank.");
        }
        Objects.requireNonNull(baseStyle, "baseStyle must not be null");
        Objects.requireNonNull(formatting, "formatting must not be null");
    }

    public static DocxRun of(String text, StyleRule baseStyle) {
        return new DocxRun(text, baseStyle, InlineFormatting.none());
    }
}
