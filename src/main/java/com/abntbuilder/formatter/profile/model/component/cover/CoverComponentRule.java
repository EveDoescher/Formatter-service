package com.abntbuilder.formatter.profile.model.component.cover;

import com.abntbuilder.formatter.profile.model.component.ComponentRule;
import com.abntbuilder.formatter.profile.model.component.ComponentContentBindings;

import java.util.Objects;

public record CoverComponentRule(
        String componentId,
        ComponentContentBindings contentBindings,
        CoverStyleMapping styleMapping,
        CoverLayoutRule layoutRule
) implements ComponentRule {

    public CoverComponentRule {
        requireNonBlank(componentId, "componentId");
        Objects.requireNonNull(contentBindings, "contentBindings must not be null");
        Objects.requireNonNull(styleMapping, "styleMapping must not be null");
        Objects.requireNonNull(layoutRule, "layoutRule must not be null");
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
