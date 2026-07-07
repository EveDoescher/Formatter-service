package com.abntbuilder.formatter.engine.model.output;

import com.abntbuilder.formatter.engine.model.profile.PageNumberingPlacement;
import com.abntbuilder.formatter.engine.model.profile.StyleRule;

import java.math.BigDecimal;
import java.util.Objects;

public record DocxPageNumbering(
        StyleRule styleRule,
        PageNumberingPlacement placement,
        boolean countingStarts,
        boolean visible,
        BigDecimal verticalDistanceFromPageEdgeCm,
        BigDecimal horizontalDistanceFromPageEdgeCm
) {

    public DocxPageNumbering {
        Objects.requireNonNull(styleRule, "styleRule must not be null");
        Objects.requireNonNull(placement, "placement must not be null");
        Objects.requireNonNull(verticalDistanceFromPageEdgeCm, "verticalDistanceFromPageEdgeCm must not be null");
        Objects.requireNonNull(horizontalDistanceFromPageEdgeCm, "horizontalDistanceFromPageEdgeCm must not be null");

        if (verticalDistanceFromPageEdgeCm.signum() < 0) {
            throw new IllegalArgumentException("verticalDistanceFromPageEdgeCm must not be negative.");
        }

        if (horizontalDistanceFromPageEdgeCm.signum() < 0) {
            throw new IllegalArgumentException("horizontalDistanceFromPageEdgeCm must not be negative.");
        }
    }
}
