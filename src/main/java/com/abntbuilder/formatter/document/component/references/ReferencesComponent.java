package com.abntbuilder.formatter.document.component.references;

import com.abntbuilder.formatter.document.component.ComponentType;
import com.abntbuilder.formatter.document.component.DocumentComponent;

import java.util.List;
import java.util.Objects;

public record ReferencesComponent(List<ReferenceEntry> entries) implements DocumentComponent {
    public ReferencesComponent {
        Objects.requireNonNull(entries, "entries must not be null");
        if (entries.isEmpty()) throw new IllegalArgumentException("entries must not be empty.");
        entries = List.copyOf(entries);
    }

    @Override
    public ComponentType type() { return ComponentType.REFERENCES; }
}
