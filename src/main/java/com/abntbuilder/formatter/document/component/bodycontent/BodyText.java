package com.abntbuilder.formatter.document.component.bodycontent;

public record BodyText(
        String text
) implements BodyInline {

    public BodyText {
        requireNonBlank(text, "text");
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
