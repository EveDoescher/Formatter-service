package com.abntbuilder.formatter.profile.model.component.indexlist;

import com.abntbuilder.formatter.profile.model.component.ComponentRule;
import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;

import java.util.Map;

public record IndexListComponentRule(
        String componentId,
        String headingStyleId,
        String headingText,
        String entryStyleId,
        String entryTemplate
) implements ComponentRule {
    public IndexListComponentRule {
        requireNonBlank(componentId, "componentId");
        requireNonBlank(headingStyleId, "headingStyleId");
        requireNonBlank(headingText, "headingText");
        requireNonBlank(entryStyleId, "entryStyleId");
        requireNonBlank(entryTemplate, "entryTemplate");
        if (!entryTemplate.contains("{number}") || !entryTemplate.contains("{caption}")) {
            throw new InvalidProfileStructureException(
                    "indexList.entryTemplate must contain {number} and {caption}.");
        }
    }

    public Map<String, String> contentBindings() { return Map.of(); }

    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
