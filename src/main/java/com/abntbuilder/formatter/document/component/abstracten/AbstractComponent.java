package com.abntbuilder.formatter.document.component.abstracten;

import com.abntbuilder.formatter.document.component.ComponentType;
import com.abntbuilder.formatter.document.component.DocumentComponent;

import java.util.List;
import java.util.Objects;

public record AbstractComponent(
        List<AbstractEntry> entries
) implements DocumentComponent {
    public AbstractComponent {
        Objects.requireNonNull(entries, "entries must not be null");
        if (entries.isEmpty()) throw new IllegalArgumentException("entries must not be empty.");
        entries = List.copyOf(entries);
    }

    @Override
    public ComponentType type() { return ComponentType.ABSTRACT_EN; }
}
