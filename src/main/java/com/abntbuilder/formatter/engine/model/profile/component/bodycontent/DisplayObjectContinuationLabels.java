package com.abntbuilder.formatter.engine.model.profile.component.bodycontent;

import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;

public record DisplayObjectContinuationLabels(
        String first,
        String middle,
        String last
) {

    public DisplayObjectContinuationLabels {
        requireNonBlank(first, "first");
        requireNonBlank(middle, "middle");
        requireNonBlank(last, "last");
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new InvalidProfileStructureException("figure continuation label " + fieldName + " must not be blank.");
        }
    }
}
