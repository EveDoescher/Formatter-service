package com.abntbuilder.formatter.engine.model.profile.layout.singlepage;

import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;

import java.util.Objects;
import java.util.Optional;

public record SinglePageItemRule(
        String id,
        boolean required,
        Optional<Integer> maxVisualLinesPerValue,
        HorizontalPlacementRule horizontalPlacement,
        int blankLinesAfter
) {

    public SinglePageItemRule {
        requireNonBlank(id, "id");
        Objects.requireNonNull(maxVisualLinesPerValue, "maxVisualLinesPerValue must not be null");
        Objects.requireNonNull(horizontalPlacement, "horizontalPlacement must not be null");
        maxVisualLinesPerValue.ifPresent(maxLines -> {
            if (maxLines <= 0) {
                throw new InvalidProfileStructureException("maxVisualLinesPerValue must be greater than zero.");
            }
        });

        if (blankLinesAfter < 0) {
            throw new InvalidProfileStructureException("blankLinesAfter must not be negative.");
        }
    }

    public SinglePageItemRule(
            String id,
            boolean required,
            Optional<Integer> maxVisualLinesPerValue,
            HorizontalPlacementRule horizontalPlacement
    ) {
        this(
                id,
                required,
                maxVisualLinesPerValue,
                horizontalPlacement,
                0
        );
    }

    public SinglePageItemRule(
            String id,
            boolean required,
            Optional<Integer> maxVisualLinesPerValue
    ) {
        this(
                id,
                required,
                maxVisualLinesPerValue,
                HorizontalPlacementRule.fullContentWidth(),
                0
        );
    }

    public SinglePageItemRule(
            String id,
            boolean required,
            Integer maxVisualLinesPerValue
    ) {
        this(
                id,
                required,
                Optional.ofNullable(maxVisualLinesPerValue),
                HorizontalPlacementRule.fullContentWidth(),
                0
        );
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new InvalidProfileStructureException(fieldName + " must not be blank.");
        }
    }
}
