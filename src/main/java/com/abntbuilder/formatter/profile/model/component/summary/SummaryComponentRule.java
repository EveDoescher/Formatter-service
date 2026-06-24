package com.abntbuilder.formatter.profile.model.component.summary;

import com.abntbuilder.formatter.profile.model.component.ComponentRule;

import java.util.List;
import java.util.Map;

public record SummaryComponentRule(
        String componentId,
        String headingStyleId,
        String headingText,
        List<String> entryStyleIdsByLevel,
        boolean useTocField
) implements ComponentRule {
    public SummaryComponentRule {
        requireNonBlank(componentId, "componentId");
        requireNonBlank(headingStyleId, "headingStyleId");
        requireNonBlank(headingText, "headingText");
        if (entryStyleIdsByLevel == null || entryStyleIdsByLevel.isEmpty())
            throw new IllegalArgumentException("entryStyleIdsByLevel must not be empty.");
        entryStyleIdsByLevel = List.copyOf(entryStyleIdsByLevel);
    }

    public Map<String, String> contentBindings() { return Map.of(); }

    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
