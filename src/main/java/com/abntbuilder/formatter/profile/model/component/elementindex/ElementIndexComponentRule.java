package com.abntbuilder.formatter.profile.model.component.elementindex;

import com.abntbuilder.formatter.profile.model.component.ComponentRule;
import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;

import java.util.Map;
import java.util.Objects;

public record ElementIndexComponentRule(
        String componentId,
        ElementType elementType,
        String headingStyleId,
        String headingText,
        String entryStyleId,
        String entryTemplate,
        int blankLinesAfterHeading
) implements ComponentRule {

    public ElementIndexComponentRule {
        requireNonBlank(componentId, "componentId");
        Objects.requireNonNull(elementType, "elementType must not be null");
        requireNonBlank(headingStyleId, "headingStyleId");
        requireNonBlank(headingText, "headingText");
        requireNonBlank(entryStyleId, "entryStyleId");
        requireNonBlank(entryTemplate, "entryTemplate");
        if (!entryTemplate.contains("{number}") || !entryTemplate.contains("{caption}")) {
            throw new InvalidProfileStructureException(
                    "elementIndex.entryTemplate must contain {number} and {caption}.");
        }
        if (blankLinesAfterHeading < 0)
            throw new IllegalArgumentException("blankLinesAfterHeading must be >= 0.");
    }

    public Map<String, String> contentBindings() { return Map.of(); }

    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
