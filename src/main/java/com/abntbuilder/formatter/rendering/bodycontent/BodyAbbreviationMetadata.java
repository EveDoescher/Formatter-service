package com.abntbuilder.formatter.rendering.bodycontent;

public record BodyAbbreviationMetadata(
        String abbreviation,
        String expansion
) {
    public BodyAbbreviationMetadata {
        if (abbreviation == null || abbreviation.isBlank()) throw new IllegalArgumentException("abbreviation must not be blank.");
        if (expansion == null || expansion.isBlank()) throw new IllegalArgumentException("expansion must not be blank.");
    }
}
