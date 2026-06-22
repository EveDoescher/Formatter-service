package com.abntbuilder.formatter.profile.model.component.bodycontent;

import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;

import java.util.Objects;

public record ChartRule(
        String captionStyleId,
        String sourceStyleId,
        String captionTemplate,
        String sourceTemplate,
        DisplayObjectContinuationLabels continuationLabels,
        DisplayObjectSourcePlacement sourcePlacement,
        FigureRule imageRule
) {

    public ChartRule {
        requireNonBlank(captionStyleId, "captionStyleId");
        requireNonBlank(sourceStyleId, "sourceStyleId");
        requireNonBlank(captionTemplate, "captionTemplate");
        requireNonBlank(sourceTemplate, "sourceTemplate");
        Objects.requireNonNull(continuationLabels, "continuationLabels must not be null");
        Objects.requireNonNull(sourcePlacement, "sourcePlacement must not be null");
        Objects.requireNonNull(imageRule, "imageRule must not be null");

        if (!captionTemplate.contains("{number}") || !captionTemplate.contains("{caption}")) {
            throw new InvalidProfileStructureException(
                    "chart.captionTemplate must contain {number} and {caption}."
            );
        }
        if (!sourceTemplate.contains("{source}")) {
            throw new InvalidProfileStructureException(
                    "chart.sourceTemplate must contain {source}."
            );
        }
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new InvalidProfileStructureException("chart." + fieldName + " must not be blank.");
        }
    }
}
