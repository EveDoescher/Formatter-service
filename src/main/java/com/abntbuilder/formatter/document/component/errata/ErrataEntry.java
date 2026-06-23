package com.abntbuilder.formatter.document.component.errata;

public record ErrataEntry(
        String page,
        String line,
        String incorrectText,
        String correctText
) {
    public ErrataEntry {
        requireNonBlank(page, "page");
        requireNonBlank(line, "line");
        requireNonBlank(incorrectText, "incorrectText");
        requireNonBlank(correctText, "correctText");
    }

    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
