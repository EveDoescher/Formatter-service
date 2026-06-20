package com.abntbuilder.formatter.document.component.bodycontent;

import java.util.Objects;
import java.util.Optional;

public record InlineFormatting(
        Optional<Boolean> bold,
        Optional<Boolean> italic,
        Optional<Boolean> underline,
        Optional<Boolean> superscript,
        Optional<Boolean> subscript
) {

    public InlineFormatting {
        Objects.requireNonNull(bold, "bold must not be null");
        Objects.requireNonNull(italic, "italic must not be null");
        Objects.requireNonNull(underline, "underline must not be null");
        Objects.requireNonNull(superscript, "superscript must not be null");
        Objects.requireNonNull(subscript, "subscript must not be null");
        if (superscript.orElse(false) && subscript.orElse(false)) {
            throw new IllegalArgumentException("superscript and subscript cannot both be true.");
        }
    }

    public static InlineFormatting none() {
        return new InlineFormatting(
                Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty()
        );
    }
}
