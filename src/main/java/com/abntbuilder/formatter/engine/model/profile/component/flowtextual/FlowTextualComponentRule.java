package com.abntbuilder.formatter.engine.model.profile.component.flowtextual;

import com.abntbuilder.formatter.engine.model.profile.component.ComponentRule;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record FlowTextualComponentRule(
        String componentId,
        boolean required,
        String description,
        List<FlowItem> items
) implements ComponentRule {

    public FlowTextualComponentRule {
        if (componentId == null || componentId.isBlank()) {
            throw new IllegalArgumentException("FlowTextualComponentRule.componentId must not be blank.");
        }
        Objects.requireNonNull(items, "FlowTextualComponentRule.items must not be null.");
        if (items.isEmpty()) {
            throw new IllegalArgumentException("FlowTextualComponentRule.items must not be empty.");
        }
        items = List.copyOf(items);
    }

    public Map<String, String> contentBindings() {
        return Map.of();
    }
}
