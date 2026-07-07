package com.abntbuilder.formatter.engine.model.content.bodycontent;

import java.util.Objects;

public record BodyText(
        String text,
        InlineFormatting formatting
) implements BodyInline {

    public BodyText(String text) {
        this(text, InlineFormatting.none());
    }

    public BodyText {
        requireNonBlank(text, "text");
        Objects.requireNonNull(formatting, "formatting must not be null");
    }

    @Override
    public String renderedText() {
        return text;
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
