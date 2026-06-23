package com.abntbuilder.formatter.profile.model.component.dedication;

import com.abntbuilder.formatter.profile.model.component.ComponentRule;

import java.util.Map;

public record DedicationComponentRule(
        String componentId,
        String textStyleId,
        int blankLinesBefore
) implements ComponentRule {
    public DedicationComponentRule {
        requireNonBlank(componentId, "componentId");
        requireNonBlank(textStyleId, "textStyleId");
        if (blankLinesBefore < 0) throw new IllegalArgumentException("blankLinesBefore must be >= 0.");
    }

    public Map<String, String> contentBindings() { return Map.of(); }

    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
