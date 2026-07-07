package com.abntbuilder.formatter.engine.model.content.sectioned;

import com.abntbuilder.formatter.engine.model.content.ComponentType;
import com.abntbuilder.formatter.engine.model.content.DocumentComponent;

import java.util.List;
import java.util.Objects;

public record SectionedContent(
        String componentId,
        List<SectionedItem> items
) implements DocumentComponent {

    public SectionedContent {
        if (componentId == null || componentId.isBlank())
            throw new IllegalArgumentException("componentId must not be blank.");
        Objects.requireNonNull(items, "items must not be null");
        if (items.isEmpty()) throw new IllegalArgumentException("items must not be empty.");
        items = List.copyOf(items);
    }

    @Override
    public ComponentType type() { return ComponentType.SECTIONED; }
}
