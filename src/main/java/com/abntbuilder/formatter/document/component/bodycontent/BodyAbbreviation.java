package com.abntbuilder.formatter.document.component.bodycontent;

public record BodyAbbreviation(
        String abbreviation,
        String expansion
) implements BodyInline {
    public BodyAbbreviation {
        requireNonBlank(abbreviation, "abbreviation");
        requireNonBlank(expansion, "expansion");
    }

    @Override
    public String renderedText() {
        return abbreviation;
    }

    private static void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank.");
        }
    }
}
