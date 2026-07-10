package com.abntbuilder.formatter.engine.model.profile.component.bodycontent;

import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;

import java.util.Set;

public record BodyContentLayoutRule(
        int blankLinesBeforeSectionTitleWhenPrecededByContent,
        int blankLinesAfterSectionTitle,
        boolean pageBreakBeforePrimarySection,
        boolean keepWithNextOnHeadings,
        String blankLineStyleId,
        Set<Integer> inlineHeadingLevels
) {

    public BodyContentLayoutRule {
        requireNonNegative(
                blankLinesBeforeSectionTitleWhenPrecededByContent,
                "blankLinesBeforeSectionTitleWhenPrecededByContent"
        );
        requireNonNegative(blankLinesAfterSectionTitle, "blankLinesAfterSectionTitle");
        requireNonBlank(blankLineStyleId, "blankLineStyleId");
        inlineHeadingLevels = inlineHeadingLevels == null ? Set.of() : Set.copyOf(inlineHeadingLevels);
    }

    public boolean isInlineHeadingLevel(int level) {
        return inlineHeadingLevels.contains(level);
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
