package com.abntbuilder.formatter.engine.model.profile.component.references;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record ReferencesFormattingRule(
        AuthorFormatRule authorFormat,
        Map<String, List<EntrySegmentRule>> entryFormats,
        Map<String, List<EntrySegmentRule>> noteFormats,
        Map<String, List<EntrySegmentRule>> shortNoteFormats,
        boolean ibidEnabled
) {
    public ReferencesFormattingRule {
        Objects.requireNonNull(authorFormat, "authorFormat must not be null");
        Objects.requireNonNull(entryFormats, "entryFormats must not be null");
        Objects.requireNonNull(noteFormats, "noteFormats must not be null");
        Objects.requireNonNull(shortNoteFormats, "shortNoteFormats must not be null");
        entryFormats = Map.copyOf(entryFormats);
        noteFormats = Map.copyOf(noteFormats);
        shortNoteFormats = Map.copyOf(shortNoteFormats);
    }

    public ReferencesFormattingRule(
            AuthorFormatRule authorFormat,
            Map<String, List<EntrySegmentRule>> entryFormats
    ) {
        this(authorFormat, entryFormats, Map.of(), Map.of(), false);
    }
}
