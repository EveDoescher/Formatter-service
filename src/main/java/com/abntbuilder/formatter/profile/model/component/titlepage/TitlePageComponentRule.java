package com.abntbuilder.formatter.profile.model.component.titlepage;

import com.abntbuilder.formatter.profile.model.component.ComponentRule;
import com.abntbuilder.formatter.profile.model.component.ComponentContentBindings;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageLayoutRule;

import java.util.Objects;

public record TitlePageComponentRule(
        String componentId,
        ComponentContentBindings contentBindings,
        TitlePageStyleMapping styleMapping,
        TitlePageTextTemplateRule textTemplates,
        SinglePageLayoutRule layoutRule
) implements ComponentRule {

    public TitlePageComponentRule {
        requireNonBlank(componentId, "componentId");
        Objects.requireNonNull(contentBindings, "contentBindings must not be null");
        Objects.requireNonNull(styleMapping, "styleMapping must not be null");
        Objects.requireNonNull(textTemplates, "textTemplates must not be null");
        Objects.requireNonNull(layoutRule, "layoutRule must not be null");
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
