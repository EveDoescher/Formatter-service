package com.abntbuilder.formatter.profile.model.component.bodycontent;

import com.abntbuilder.formatter.profile.model.TextAlignment;
import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;

import java.math.BigDecimal;
import java.util.Objects;

public record TableRule(
        String captionStyleId,
        String sourceStyleId,
        String headerStyleId,
        String cellStyleId,
        String captionTemplate,
        String sourceTemplate,
        DisplayObjectContinuationLabels continuationLabels,
        DisplayObjectSourcePlacement sourcePlacement,
        TextAlignment tableAlignment,
        BigDecimal widthPercent,
        Boolean repeatHeaderOnPageBreak,
        NumberingStrategy numberingStrategy,
        String label,
        String separator
) {

    public TableRule {
        requireNonBlank(captionStyleId, "captionStyleId");
        requireNonBlank(sourceStyleId, "sourceStyleId");
        requireNonBlank(headerStyleId, "headerStyleId");
        requireNonBlank(cellStyleId, "cellStyleId");
        requireNonBlank(captionTemplate, "captionTemplate");
        requireNonBlank(sourceTemplate, "sourceTemplate");
        Objects.requireNonNull(continuationLabels, "continuationLabels must not be null");
        Objects.requireNonNull(sourcePlacement, "sourcePlacement must not be null");
        Objects.requireNonNull(tableAlignment, "tableAlignment must not be null");
        requirePositive(widthPercent, "widthPercent");
        Objects.requireNonNull(repeatHeaderOnPageBreak, "repeatHeaderOnPageBreak must not be null");
        Objects.requireNonNull(numberingStrategy, "numberingStrategy must not be null");
        requireNonBlank(label, "label");

        if (widthPercent.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new InvalidProfileStructureException("table.widthPercent must be less than or equal to 100.");
        }

        if (!captionTemplate.contains("{number}") || !captionTemplate.contains("{caption}")) {
            throw new InvalidProfileStructureException(
                    "table.captionTemplate must contain {number} and {caption}."
            );
        }

        if (!sourceTemplate.contains("{source}")) {
            throw new InvalidProfileStructureException("table.sourceTemplate must contain {source}.");
        }
    }

    private static void requirePositive(BigDecimal value, String fieldName) {
        if (value == null || value.signum() <= 0) {
            throw new InvalidProfileStructureException("table." + fieldName + " must be greater than zero.");
        }
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new InvalidProfileStructureException("table." + fieldName + " must not be blank.");
        }
    }
}
