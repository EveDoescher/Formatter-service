package com.abntbuilder.formatter.profile.model.component.references;

import com.abntbuilder.formatter.profile.model.component.ComponentRule;

import java.util.Map;
import java.util.Objects;

public record ReferencesComponentRule(
        String componentId,
        String headingStyleId,
        String headingText,
        String entryStyleId,
        int blankLinesBetweenEntries,
        ReferencesFormattingRule formattingRule
) implements ComponentRule {
    public ReferencesComponentRule {
        requireNonBlank(componentId, "componentId");
        requireNonBlank(headingStyleId, "headingStyleId");
        requireNonBlank(headingText, "headingText");
        requireNonBlank(entryStyleId, "entryStyleId");
        if (blankLinesBetweenEntries < 0) throw new IllegalArgumentException("blankLinesBetweenEntries must be >= 0.");
        Objects.requireNonNull(formattingRule, "formattingRule must not be null");
    }

    public Map<String, String> contentBindings() { return Map.of(); }

    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
