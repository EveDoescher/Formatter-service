package com.abntbuilder.formatter.engine.model.profile.component.bodycontent;

import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;

public record BodyContentNumberingRule(
        boolean enabled,
        String separator,
        String primarySuffix
) {

    public BodyContentNumberingRule {
        if (enabled) {
            requireNonBlank(separator, "separator");
            requireNonNull(primarySuffix, "primarySuffix");
        }
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new InvalidProfileStructureException(fieldName + " must not be blank.");
        }
    }

    private static void requireNonNull(String value, String fieldName) {
        if (value == null) {
            throw new InvalidProfileStructureException(fieldName + " must not be null.");
        }
    }
}
