package com.abntbuilder.formatter.document.component.bodycontent;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record BodySection(
        String id,
        int level,
        Optional<String> title,
        List<String> paragraphs
) {

    public BodySection {
        requireNonBlank(id, "id");
        if (level < 1 || level > 6) {
            throw new IllegalArgumentException("level must be between 1 and 6.");
        }

        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(paragraphs, "paragraphs must not be null");

        title.ifPresent(value -> requireNonBlank(value, "title"));

        if (paragraphs.isEmpty()) {
            throw new IllegalArgumentException("paragraphs must not be empty.");
        }

        paragraphs = List.copyOf(paragraphs);

        for (String paragraph : paragraphs) {
            requireNonBlank(paragraph, "paragraphs item");
        }
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
