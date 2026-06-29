package com.abntbuilder.formatter.document.component.abstracten;

import java.util.List;
import java.util.Objects;

public record AbstractEntry(
        String headingText,
        String text,
        List<String> keywords,
        String keywordsLabel
) {
    public AbstractEntry {
        requireNonBlank(headingText, "headingText");
        requireNonBlank(text, "text");
        Objects.requireNonNull(keywords, "keywords must not be null");
        if (keywords.isEmpty()) throw new IllegalArgumentException("keywords must not be empty.");
        keywords = List.copyOf(keywords);
        requireNonBlank(keywordsLabel, "keywordsLabel");
    }

    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
