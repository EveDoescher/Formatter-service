package com.abntbuilder.formatter.engine.model.profile.component.references;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record ReferencesFormattingRule(
        AuthorFormatRule authorFormat,
        Map<String, List<EntrySegmentRule>> entryFormats
) {
    public ReferencesFormattingRule {
        Objects.requireNonNull(authorFormat, "authorFormat must not be null");
        Objects.requireNonNull(entryFormats, "entryFormats must not be null");
        entryFormats = Map.copyOf(entryFormats);
    }
}
