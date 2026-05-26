package com.abntbuilder.formatter.output.docx.api;

import com.abntbuilder.formatter.profile.model.StyleRule;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

public record DocxBlankLine(
        StyleRule styleRule,
        Optional<BigDecimal> exactLineHeightPt
) implements DocxBlock {

    public DocxBlankLine(StyleRule styleRule) {
        this(styleRule, Optional.empty());
    }

    public DocxBlankLine {
        Objects.requireNonNull(styleRule, "styleRule must not be null");
        Objects.requireNonNull(exactLineHeightPt, "exactLineHeightPt must not be null");

        exactLineHeightPt.ifPresent(value -> {
            if (value.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("exactLineHeightPt must be greater than zero.");
            }
        });
    }
}