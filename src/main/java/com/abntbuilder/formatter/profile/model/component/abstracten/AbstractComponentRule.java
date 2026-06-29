package com.abntbuilder.formatter.profile.model.component.abstracten;

import com.abntbuilder.formatter.profile.model.component.ComponentRule;

import java.util.Map;

public record AbstractComponentRule(
        String componentId,
        String headingStyleId,
        String textStyleId,
        String keywordsStyleId,
        String keywordsSeparator,
        String keywordsTerminator,
        int blankLinesAfterHeading
) implements ComponentRule {
    public AbstractComponentRule {
        requireNonBlank(componentId, "componentId");
        requireNonBlank(headingStyleId, "headingStyleId");
        requireNonBlank(textStyleId, "textStyleId");
        requireNonBlank(keywordsStyleId, "keywordsStyleId");
        requireNonBlank(keywordsSeparator, "keywordsSeparator");
        requireNonBlank(keywordsTerminator, "keywordsTerminator");
        if (blankLinesAfterHeading < 0) throw new IllegalArgumentException("blankLinesAfterHeading must be >= 0.");
    }

    public Map<String, String> contentBindings() { return Map.of(); }

    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
