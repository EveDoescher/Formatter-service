package com.abntbuilder.formatter.document.component.bodycontent;

import java.util.Objects;

public record BodyQuoteText(
        BodyQuoteType type,
        String text,
        InlineFormatting formatting
) implements BodyInline {

    public BodyQuoteText(BodyQuoteType type, String text) {
        this(type, text, InlineFormatting.none());
    }

    public BodyQuoteText {
        Objects.requireNonNull(type, "type must not be null");
        requireNonBlank(text, "text");
        Objects.requireNonNull(formatting, "formatting must not be null");
    }

    @Override
    public String renderedText() {
        return switch (type) {
            case SHORT -> "\"" + text.trim() + "\"";
        };
    }

    private static void requireNoBoundaryQuotes(String value) {
        String trimmed = value.trim();
        if (trimmed.length() >= 2 && trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            throw new IllegalArgumentException(
                    "manual boundary quotation marks must not be provided for SHORT quote text."
            );
        }
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
        requireNoBoundaryQuotes(value);
    }
}
