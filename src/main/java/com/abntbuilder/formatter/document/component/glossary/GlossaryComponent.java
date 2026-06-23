package com.abntbuilder.formatter.document.component.glossary;

import com.abntbuilder.formatter.document.component.ComponentType;
import com.abntbuilder.formatter.document.component.DocumentComponent;

import java.util.List;
import java.util.Objects;

public record GlossaryComponent(List<GlossaryEntry> entries) implements DocumentComponent {
    public GlossaryComponent {
        Objects.requireNonNull(entries, "entries must not be null");
        if (entries.isEmpty()) throw new IllegalArgumentException("entries must not be empty.");
        entries = List.copyOf(entries);
    }

    @Override
    public ComponentType type() { return ComponentType.GLOSSARY; }
}
