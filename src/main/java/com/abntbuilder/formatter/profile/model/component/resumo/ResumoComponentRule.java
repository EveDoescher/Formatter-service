package com.abntbuilder.formatter.profile.model.component.resumo;

import com.abntbuilder.formatter.profile.model.component.ComponentRule;

import java.util.Map;

public record ResumoComponentRule(
        String componentId,
        String headingStyleId,
        String headingText,
        String textStyleId,
        String keywordsStyleId,
        String keywordsLabel,
        String keywordsSeparator
) implements ComponentRule {
    public ResumoComponentRule {
        requireNonBlank(componentId, "componentId");
        requireNonBlank(headingStyleId, "headingStyleId");
        requireNonBlank(headingText, "headingText");
        requireNonBlank(textStyleId, "textStyleId");
        requireNonBlank(keywordsStyleId, "keywordsStyleId");
        requireNonBlank(keywordsLabel, "keywordsLabel");
        requireNonBlank(keywordsSeparator, "keywordsSeparator");
    }

    public Map<String, String> contentBindings() { return Map.of(); }

    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
