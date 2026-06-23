package com.abntbuilder.formatter.profile.model.component.epigraph;

import com.abntbuilder.formatter.profile.model.component.ComponentRule;

import java.util.Map;

public record EpigraphComponentRule(
        String componentId,
        String textStyleId,
        String authorStyleId,
        String authorTemplate
) implements ComponentRule {
    public EpigraphComponentRule {
        requireNonBlank(componentId, "componentId");
        requireNonBlank(textStyleId, "textStyleId");
        requireNonBlank(authorStyleId, "authorStyleId");
        requireNonBlank(authorTemplate, "authorTemplate");
    }

    public Map<String, String> contentBindings() { return Map.of(); }

    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
