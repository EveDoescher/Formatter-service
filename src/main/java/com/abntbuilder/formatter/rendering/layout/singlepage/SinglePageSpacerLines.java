package com.abntbuilder.formatter.rendering.layout.singlepage;

import com.abntbuilder.formatter.profile.model.StyleRule;

import java.util.Objects;

public record SinglePageSpacerLines(
        String gapId,
        String fromGroupId,
        String toGroupId,
        int lineCount,
        StyleRule styleRule
) implements SinglePageLayoutElement {

    public SinglePageSpacerLines {
        requireNonBlank(gapId, "gapId");
        requireNonBlank(fromGroupId, "fromGroupId");
        requireNonBlank(toGroupId, "toGroupId");
        Objects.requireNonNull(styleRule, "styleRule must not be null");

        if (lineCount <= 0) {
            throw new IllegalArgumentException("lineCount must be greater than zero.");
        }
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
