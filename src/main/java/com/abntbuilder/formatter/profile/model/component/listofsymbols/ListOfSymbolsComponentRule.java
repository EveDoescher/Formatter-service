package com.abntbuilder.formatter.profile.model.component.listofsymbols;

import com.abntbuilder.formatter.profile.model.component.ComponentRule;

import java.util.Map;

public record ListOfSymbolsComponentRule(
        String componentId,
        String headingStyleId,
        String headingText,
        String entryStyleId,
        String termSeparator
) implements ComponentRule {
    public ListOfSymbolsComponentRule {
        requireNonBlank(componentId, "componentId");
        requireNonBlank(headingStyleId, "headingStyleId");
        requireNonBlank(headingText, "headingText");
        requireNonBlank(entryStyleId, "entryStyleId");
        requireNonBlank(termSeparator, "termSeparator");
    }

    public Map<String, String> contentBindings() { return Map.of(); }

    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
