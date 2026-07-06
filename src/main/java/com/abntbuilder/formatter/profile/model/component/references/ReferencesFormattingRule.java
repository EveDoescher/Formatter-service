package com.abntbuilder.formatter.profile.model.component.references;

import com.abntbuilder.formatter.document.component.references.ReferenceType;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record ReferencesFormattingRule(
        AuthorFormatRule authorFormat,
        Map<ReferenceType, List<EntrySegmentRule>> entryFormats
) {
    public ReferencesFormattingRule {
        Objects.requireNonNull(authorFormat, "authorFormat must not be null");
        Objects.requireNonNull(entryFormats, "entryFormats must not be null");
        entryFormats = Map.copyOf(entryFormats);
    }
}
