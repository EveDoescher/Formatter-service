package com.abntbuilder.formatter.engine.model.profile.component.references;

public record EntrySegmentRule(
        String source,
        boolean bold,
        String prefix,
        String suffix,
        boolean optional
) {
    public EntrySegmentRule {
        if (source == null || source.isBlank()) throw new IllegalArgumentException("source must not be blank.");
        if (prefix == null) throw new IllegalArgumentException("prefix must not be null.");
        if (suffix == null) throw new IllegalArgumentException("suffix must not be null.");
    }
}
