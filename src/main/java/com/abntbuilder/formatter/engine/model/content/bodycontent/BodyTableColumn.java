package com.abntbuilder.formatter.engine.model.content.bodycontent;

public record BodyTableColumn(
        String header
) {

    public BodyTableColumn {
        requireNonBlank(header, "header");
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
