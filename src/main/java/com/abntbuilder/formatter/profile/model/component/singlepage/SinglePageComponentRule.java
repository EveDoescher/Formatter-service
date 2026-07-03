package com.abntbuilder.formatter.profile.model.component.singlepage;

import com.abntbuilder.formatter.profile.model.component.ComponentRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageLayoutRule;

import java.util.Map;
import java.util.Objects;

public record SinglePageComponentRule(
        String componentId,
        Map<String, SlotRule> slots,
        Map<String, String> styleMapping,
        SinglePageLayoutRule layoutRule
) implements ComponentRule {

    public SinglePageComponentRule {
        if (componentId == null || componentId.isBlank()) {
            throw new IllegalArgumentException("SinglePageComponentRule.componentId must not be blank.");
        }
        Objects.requireNonNull(slots, "SinglePageComponentRule.slots must not be null.");
        Objects.requireNonNull(styleMapping, "SinglePageComponentRule.styleMapping must not be null.");
        Objects.requireNonNull(layoutRule, "SinglePageComponentRule.layoutRule must not be null.");
        slots = Map.copyOf(slots);
        styleMapping = Map.copyOf(styleMapping);
    }
}
