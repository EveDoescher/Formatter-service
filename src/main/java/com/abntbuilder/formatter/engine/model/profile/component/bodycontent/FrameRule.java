package com.abntbuilder.formatter.engine.model.profile.component.bodycontent;

import com.abntbuilder.formatter.engine.model.profile.TextAlignment;

import java.math.BigDecimal;
import java.util.Objects;

public record FrameRule(
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
    public FrameRule {
        requireNonBlank(captionStyleId, "captionStyleId");
        requireNonBlank(sourceStyleId, "sourceStyleId");
        requireNonBlank(headerStyleId, "headerStyleId");
        requireNonBlank(cellStyleId, "cellStyleId");
        requireNonBlank(captionTemplate, "captionTemplate");
        requireNonBlank(sourceTemplate, "sourceTemplate");
        Objects.requireNonNull(continuationLabels, "continuationLabels must not be null");
        Objects.requireNonNull(sourcePlacement, "sourcePlacement must not be null");
        Objects.requireNonNull(tableAlignment, "tableAlignment must not be null");
        Objects.requireNonNull(widthPercent, "widthPercent must not be null");
        if (widthPercent.compareTo(BigDecimal.ZERO) <= 0 || widthPercent.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("widthPercent must be between 0 (exclusive) and 100 (inclusive).");
        }
        Objects.requireNonNull(repeatHeaderOnPageBreak, "repeatHeaderOnPageBreak must not be null");
        Objects.requireNonNull(numberingStrategy, "numberingStrategy must not be null");
        requireNonBlank(label, "label");
    }

    private static void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank.");
        }
    }
}
