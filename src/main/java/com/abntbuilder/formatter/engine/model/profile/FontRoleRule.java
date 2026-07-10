package com.abntbuilder.formatter.engine.model.profile;

import java.util.List;
import java.util.Objects;

public record FontRoleRule(
        String defaultFont,
        List<String> allowedValues,
        List<String> styleIds
) {
    public FontRoleRule {
        requireNonBlank(defaultFont, "defaultFont");
        Objects.requireNonNull(allowedValues, "allowedValues must not be null");
        Objects.requireNonNull(styleIds, "styleIds must not be null");

        if (styleIds.isEmpty()) {
            throw new IllegalArgumentException("fontRole.styleIds must not be empty.");
        }

        allowedValues = List.copyOf(allowedValues);
        styleIds = List.copyOf(styleIds);

        if (!allowedValues.isEmpty() && !allowedValues.contains(defaultFont)) {
            throw new IllegalArgumentException(
                    "fontRole.defaultFont \"" + defaultFont + "\" must be present in allowedValues."
            );
        }
    }

    public boolean allowsChoice() {
        return !allowedValues.isEmpty();
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
