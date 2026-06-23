package com.abntbuilder.formatter.document.component.glossary;

public record GlossaryEntry(String term, String definition) {
    public GlossaryEntry {
        requireNonBlank(term, "term");
        requireNonBlank(definition, "definition");
    }

    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
