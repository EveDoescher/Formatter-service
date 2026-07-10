package com.abntbuilder.formatter.engine.model.profile.component.bodycontent;

import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;

public record BodyContentLayoutRule(
        int blankLinesBeforeSectionTitleWhenPrecededByContent,
        int blankLinesAfterSectionTitle,
        boolean pageBreakBeforePrimarySection,
        boolean keepWithNextOnHeadings,
        String blankLineStyleId
) {

    public BodyContentLayoutRule {
        requireNonNegative(
                blankLinesBeforeSectionTitleWhenPrecededByContent,
                "blankLinesBeforeSectionTitleWhenPrecededByContent"
        );
        requireNonNegative(blankLinesAfterSectionTitle, "blankLinesAfterSectionTitle");
        requireNonBlank(blankLineStyleId, "blankLineStyleId");
    }

    private static void requireNonNegative(int value, String fieldName) {
        if (value < 0) {
            throw new InvalidProfileStructureException(fieldName + " must not be negative.");
        }
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new InvalidProfileStructureException(fieldName + " must not be blank.");
        }
    }
}
