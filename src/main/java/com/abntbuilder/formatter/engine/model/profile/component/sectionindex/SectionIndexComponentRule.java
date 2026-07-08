package com.abntbuilder.formatter.engine.model.profile.component.sectionindex;

import com.abntbuilder.formatter.engine.model.profile.component.ComponentRule;

import java.util.List;
import java.util.Map;

public record SectionIndexComponentRule(
        String componentId,
        String headingStyleId,
        String headingText,
        List<String> entryStyleIdsByLevel,
        boolean useTocField,
        int blankLinesAfterHeading
) implements ComponentRule {

    public SectionIndexComponentRule {
        requireNonBlank(componentId, "componentId");
        requireNonBlank(headingStyleId, "headingStyleId");
        requireNonBlank(headingText, "headingText");
        if (entryStyleIdsByLevel == null || entryStyleIdsByLevel.isEmpty())
            throw new IllegalArgumentException("entryStyleIdsByLevel must not be empty.");
        entryStyleIdsByLevel = List.copyOf(entryStyleIdsByLevel);
        if (blankLinesAfterHeading < 0)
            throw new IllegalArgumentException("blankLinesAfterHeading must be >= 0.");
    }

    public Map<String, String> contentBindings() { return Map.of(); }

    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
