package com.abntbuilder.formatter.profile.model.component.errata;

import com.abntbuilder.formatter.profile.model.component.ComponentRule;

import java.util.Map;

public record ErrataComponentRule(
        String componentId,
        String headingStyleId,
        String headingText,
        String entryStyleId,
        String entryTemplate
) implements ComponentRule {
    public ErrataComponentRule {
        requireNonBlank(componentId, "componentId");
        requireNonBlank(headingStyleId, "headingStyleId");
        requireNonBlank(headingText, "headingText");
        requireNonBlank(entryStyleId, "entryStyleId");
        requireNonBlank(entryTemplate, "entryTemplate");
    }

    public Map<String, String> contentBindings() { return Map.of(); }

    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
