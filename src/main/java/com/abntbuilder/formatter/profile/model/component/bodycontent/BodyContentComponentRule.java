package com.abntbuilder.formatter.profile.model.component.bodycontent;

import com.abntbuilder.formatter.profile.model.component.ComponentRule;

import java.util.Objects;

public record BodyContentComponentRule(
        String componentId,
        BodyContentStyleMapping styleMapping,
        BodyContentNumberingRule numbering,
        BodyContentLayoutRule layout,
        FigureRule figure,
        TableRule table
) implements ComponentRule {

    public BodyContentComponentRule {
        requireNonBlank(componentId, "componentId");
        Objects.requireNonNull(styleMapping, "styleMapping must not be null");
        Objects.requireNonNull(numbering, "numbering must not be null");
        Objects.requireNonNull(layout, "layout must not be null");
        Objects.requireNonNull(figure, "figure must not be null");
        Objects.requireNonNull(table, "table must not be null");
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
