package com.abntbuilder.formatter.document.component.bodycontent;

import java.util.Objects;
import java.util.Optional;

public record InlineFormatting(
        Optional<Boolean> bold,
        Optional<Boolean> italic,
        Optional<Boolean> underline
) {

    public InlineFormatting {
        Objects.requireNonNull(bold, "bold must not be null");
        Objects.requireNonNull(italic, "italic must not be null");
        Objects.requireNonNull(underline, "underline must not be null");
    }

    public static InlineFormatting none() {
        return new InlineFormatting(Optional.empty(), Optional.empty(), Optional.empty());
    }
}
