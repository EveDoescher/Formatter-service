package com.abntbuilder.formatter.engine.model.profile.component.bodycontent;

import com.abntbuilder.formatter.engine.model.profile.component.ComponentRule;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record BodyContentComponentRule(
        String componentId,
        boolean required,
        String description,
        BodyContentStyleMapping styleMapping,
        BodyContentNumberingRule numbering,
        BodyContentLayoutRule layout,
        FigureRule figure,
        TableRule table,
        FrameRule frame,
        CodeListingRule codeListing,
        ChartRule chart,
        CitationFormattingRule citationFormatting,
        CrossReferenceLabelsRule crossReferenceLabels
) implements ComponentRule {

    public BodyContentComponentRule {
        requireNonBlank(componentId, "componentId");
        Objects.requireNonNull(styleMapping, "styleMapping must not be null");
        Objects.requireNonNull(numbering, "numbering must not be null");
        Objects.requireNonNull(layout, "layout must not be null");
        Objects.requireNonNull(figure, "figure must not be null");
        Objects.requireNonNull(table, "table must not be null");
        Objects.requireNonNull(frame, "frame must not be null");
        Objects.requireNonNull(codeListing, "codeListing must not be null");
        Objects.requireNonNull(chart, "chart must not be null");
        Objects.requireNonNull(citationFormatting, "citationFormatting must not be null");
        Objects.requireNonNull(crossReferenceLabels, "crossReferenceLabels must not be null");
    }

    public BodyContentComponentRule withSectionTitleStyleIds(List<String> overrideStyleIds) {
        return new BodyContentComponentRule(
                componentId,
                required,
                description,
                styleMapping.withSectionTitleStyleIds(overrideStyleIds),
                numbering, layout, figure, table, frame, codeListing, chart,
                citationFormatting, crossReferenceLabels
        );
    }

    public Map<String, String> contentBindings() {
        return Map.of();
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
