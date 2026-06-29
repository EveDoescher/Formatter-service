package com.abntbuilder.formatter.profile.model.component.glossary;

import com.abntbuilder.formatter.profile.model.component.ComponentRule;

import java.util.Map;

public record GlossaryComponentRule(
        String componentId,
        String headingStyleId,
        String headingText,
        String entryStyleId,
        String termSeparator,
        int blankLinesAfterHeading
) implements ComponentRule {
    public GlossaryComponentRule {
        requireNonBlank(componentId, "componentId");
        requireNonBlank(headingStyleId, "headingStyleId");
        requireNonBlank(headingText, "headingText");
        requireNonBlank(entryStyleId, "entryStyleId");
        requireNonBlank(termSeparator, "termSeparator");
        if (blankLinesAfterHeading < 0) throw new IllegalArgumentException("blankLinesAfterHeading must be >= 0.");
    }

    public Map<String, String> contentBindings() { return Map.of(); }

    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
