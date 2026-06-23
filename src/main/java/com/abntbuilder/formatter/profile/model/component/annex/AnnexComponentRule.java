package com.abntbuilder.formatter.profile.model.component.annex;

import com.abntbuilder.formatter.profile.model.component.ComponentRule;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record AnnexComponentRule(
        String componentId,
        String headingTemplate,
        String headingStyleId,
        String paragraphStyleId,
        List<String> sectionTitleStyleIdsByLevel
) implements ComponentRule {
    public AnnexComponentRule {
        requireNonBlank(componentId, "componentId");
        requireNonBlank(headingTemplate, "headingTemplate");
        requireNonBlank(headingStyleId, "headingStyleId");
        requireNonBlank(paragraphStyleId, "paragraphStyleId");
        Objects.requireNonNull(sectionTitleStyleIdsByLevel, "sectionTitleStyleIdsByLevel must not be null");
        sectionTitleStyleIdsByLevel = List.copyOf(sectionTitleStyleIdsByLevel);
    }

    public Map<String, String> contentBindings() { return Map.of(); }

    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
