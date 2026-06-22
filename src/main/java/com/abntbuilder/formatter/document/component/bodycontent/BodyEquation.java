package com.abntbuilder.formatter.document.component.bodycontent;

import java.util.Objects;
import java.util.Optional;

public record BodyEquation(
        String text,
        Optional<String> label
) implements BodyBlock {

    public BodyEquation {
        requireNonBlank(text, "text");
        Objects.requireNonNull(label, "label must not be null");
    }

    private static void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank.");
        }
    }
}
