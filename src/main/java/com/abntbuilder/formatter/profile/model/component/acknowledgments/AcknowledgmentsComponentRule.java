package com.abntbuilder.formatter.profile.model.component.acknowledgments;

import com.abntbuilder.formatter.profile.model.component.ComponentRule;

import java.util.Map;

public record AcknowledgmentsComponentRule(
        String componentId,
        String headingStyleId,
        String headingText,
        String textStyleId
) implements ComponentRule {
    public AcknowledgmentsComponentRule {
        requireNonBlank(componentId, "componentId");
        requireNonBlank(headingStyleId, "headingStyleId");
        requireNonBlank(headingText, "headingText");
        requireNonBlank(textStyleId, "textStyleId");
    }

    public Map<String, String> contentBindings() { return Map.of(); }

    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
