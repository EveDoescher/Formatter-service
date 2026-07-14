package com.abntbuilder.formatter.engine.model.profile.component.sectioned;

import com.abntbuilder.formatter.engine.model.profile.component.ComponentRule;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record SectionedComponentRule(
        String componentId,
        boolean required,
        String description,
        String headingTemplate,
        String headingStyleId,
        String paragraphStyleId,
        List<String> sectionTitleStyleIdsByLevel,
        IndexingStyle indexingStyle,
        String bodyContentComponentId
) implements ComponentRule {

    public enum IndexingStyle {
        ALPHABETIC,
        ALPHABETIC_LOWER,
        NUMERIC,
        ROMAN_UPPER,
        ROMAN_LOWER
    }

    public SectionedComponentRule {
        requireNonBlank(componentId, "componentId");
        requireNonBlank(headingTemplate, "headingTemplate");
        requireNonBlank(headingStyleId, "headingStyleId");
        requireNonBlank(paragraphStyleId, "paragraphStyleId");
        Objects.requireNonNull(sectionTitleStyleIdsByLevel, "sectionTitleStyleIdsByLevel must not be null");
        Objects.requireNonNull(indexingStyle, "indexingStyle must not be null");
        requireNonBlank(bodyContentComponentId, "bodyContentComponentId");
        sectionTitleStyleIdsByLevel = List.copyOf(sectionTitleStyleIdsByLevel);
    }

    public Map<String, String> contentBindings() { return Map.of(); }

    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
