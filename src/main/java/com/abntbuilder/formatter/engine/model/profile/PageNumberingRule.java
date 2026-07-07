package com.abntbuilder.formatter.engine.model.profile;

import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;

import java.math.BigDecimal;
import java.util.Objects;

public record PageNumberingRule(
        boolean enabled,
        String countFromComponentId,
        String visibleFromComponentId,
        String styleId,
        PageNumberingPlacement placement,
        BigDecimal verticalDistanceFromPageEdgeCm,
        BigDecimal horizontalDistanceFromPageEdgeCm
) {

    public PageNumberingRule {
        if (enabled) {
            requireNonBlank(countFromComponentId, "countFromComponentId");
            requireNonBlank(visibleFromComponentId, "visibleFromComponentId");
            requireNonBlank(styleId, "styleId");
            Objects.requireNonNull(placement, "placement must not be null");
            requireNonNegative(
                    verticalDistanceFromPageEdgeCm,
                    "verticalDistanceFromPageEdgeCm"
            );
            requireNonNegative(
                    horizontalDistanceFromPageEdgeCm,
                    "horizontalDistanceFromPageEdgeCm"
            );
        }
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new InvalidProfileStructureException(fieldName + " must not be blank.");
        }
    }

    private static void requireNonNegative(BigDecimal value, String fieldName) {
        if (value == null) {
            throw new InvalidProfileStructureException(fieldName + " must not be null.");
        }

        if (value.signum() < 0) {
            throw new InvalidProfileStructureException(fieldName + " must not be negative.");
        }
    }
}
