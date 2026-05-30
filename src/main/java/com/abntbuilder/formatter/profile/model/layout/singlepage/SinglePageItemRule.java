package com.abntbuilder.formatter.profile.model.layout.singlepage;

import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;

import java.util.Objects;
import java.util.Optional;

public record SinglePageItemRule(
        String id,
        boolean required,
        Optional<Integer> maxVisualLinesPerValue
) {

    public SinglePageItemRule {
        requireNonBlank(id, "id");
        Objects.requireNonNull(maxVisualLinesPerValue, "maxVisualLinesPerValue must not be null");
        maxVisualLinesPerValue.ifPresent(maxLines -> {
            if (maxLines <= 0) {
                throw new InvalidProfileStructureException("maxVisualLinesPerValue must be greater than zero.");
            }
        });
    }

    public SinglePageItemRule(
            String id,
            boolean required,
            Integer maxVisualLinesPerValue
    ) {
        this(
                id,
                required,
                Optional.ofNullable(maxVisualLinesPerValue)
        );
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new InvalidProfileStructureException(fieldName + " must not be blank.");
        }
    }
}
