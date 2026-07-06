package com.abntbuilder.formatter.document.component.singlepage;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record EntryListValue(List<Map<String, ContentValue>> entries) implements ContentValue {
    public EntryListValue {
        Objects.requireNonNull(entries, "EntryListValue.entries must not be null.");
        if (entries.isEmpty()) throw new IllegalArgumentException("EntryListValue.entries must not be empty.");
        for (Map<String, ContentValue> entry : entries) {
            Objects.requireNonNull(entry, "EntryListValue: each entry map must not be null.");
        }
        entries = entries.stream().map(Map::copyOf).toList();
    }
}
