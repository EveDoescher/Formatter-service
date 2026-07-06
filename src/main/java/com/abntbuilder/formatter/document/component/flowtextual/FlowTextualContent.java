package com.abntbuilder.formatter.document.component.flowtextual;

import com.abntbuilder.formatter.document.component.ComponentType;
import com.abntbuilder.formatter.document.component.DocumentComponent;
import com.abntbuilder.formatter.document.component.singlepage.ContentValue;

import java.util.Map;
import java.util.Objects;

public record FlowTextualContent(
        String componentId,
        Map<String, ContentValue> slots
) implements DocumentComponent {

    public FlowTextualContent {
        if (componentId == null || componentId.isBlank()) {
            throw new IllegalArgumentException("FlowTextualContent.componentId must not be blank.");
        }
        Objects.requireNonNull(slots, "FlowTextualContent.slots must not be null.");
        slots = Map.copyOf(slots);
    }

    @Override
    public ComponentType type() {
        return ComponentType.FLOW_TEXTUAL;
    }
}
