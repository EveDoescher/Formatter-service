package com.abntbuilder.formatter.engine.model.output;

import com.abntbuilder.formatter.engine.model.profile.TextAlignment;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

public record ParagraphLayoutOverride(
        Optional<BigDecimal> leftIndentCm,
        Optional<BigDecimal> rightIndentCm,
        Optional<TextAlignment> alignment
) {

    public ParagraphLayoutOverride {
        Objects.requireNonNull(leftIndentCm, "leftIndentCm must not be null");
        Objects.requireNonNull(rightIndentCm, "rightIndentCm must not be null");
        Objects.requireNonNull(alignment, "alignment must not be null");

        leftIndentCm.ifPresent(value -> {
            if (value.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("leftIndentCm must not be negative.");
            }
        });

        rightIndentCm.ifPresent(value -> {
            if (value.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("rightIndentCm must not be negative.");
            }
        });
    }

    public static ParagraphLayoutOverride none() {
        return new ParagraphLayoutOverride(
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
    }
}
