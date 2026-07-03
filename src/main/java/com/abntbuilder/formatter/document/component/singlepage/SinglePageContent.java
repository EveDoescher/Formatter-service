package com.abntbuilder.formatter.document.component.singlepage;

import com.abntbuilder.formatter.document.component.ComponentType;
import com.abntbuilder.formatter.document.component.DocumentComponent;

import java.util.Map;
import java.util.Objects;

public record SinglePageContent(
        String componentId,
        Map<String, ContentValue> slots
) implements DocumentComponent {

    public SinglePageContent {
        if (componentId == null || componentId.isBlank()) {
            throw new IllegalArgumentException("SinglePageContent.componentId must not be blank.");
        }
        Objects.requireNonNull(slots, "SinglePageContent.slots must not be null.");
        slots = Map.copyOf(slots);
    }

    @Override
    public ComponentType type() {
        return ComponentType.SINGLE_PAGE;
    }
}
