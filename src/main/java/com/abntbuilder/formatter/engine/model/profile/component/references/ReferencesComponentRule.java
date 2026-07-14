package com.abntbuilder.formatter.engine.model.profile.component.references;

import com.abntbuilder.formatter.engine.model.profile.component.ComponentRule;

import java.util.Map;
import java.util.Objects;

public record ReferencesComponentRule(
        String componentId,
        boolean required,
        String description,
        String headingStyleId,
        String headingText,
        String entryStyleId,
        int blankLinesBetweenEntries,
        ReferencesFormattingRule formattingRule,
        int blankLinesAfterHeading,
        ReferenceSortOrder sortOrder
) implements ComponentRule {

    public enum ReferenceSortOrder {
        AS_GIVEN,
        ALPHABETICAL,
        CITATION_ORDER
    }

    public ReferencesComponentRule {
        requireNonBlank(componentId, "componentId");
        requireNonBlank(headingStyleId, "headingStyleId");
        requireNonBlank(headingText, "headingText");
        requireNonBlank(entryStyleId, "entryStyleId");
        if (blankLinesBetweenEntries < 0) throw new IllegalArgumentException("blankLinesBetweenEntries must be >= 0.");
        Objects.requireNonNull(formattingRule, "formattingRule must not be null");
        if (blankLinesAfterHeading < 0) throw new IllegalArgumentException("blankLinesAfterHeading must be >= 0.");
        if (sortOrder == null) sortOrder = ReferenceSortOrder.AS_GIVEN;
    }

    public Map<String, String> contentBindings() { return Map.of(); }

    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
