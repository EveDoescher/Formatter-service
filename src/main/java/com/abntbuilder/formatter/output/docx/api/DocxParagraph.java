package com.abntbuilder.formatter.output.docx.api;

import com.abntbuilder.formatter.profile.model.StyleRule;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

public record DocxParagraph(
        String text,
        StyleRule styleRule,
        Optional<BigDecimal> spacingBeforeOverridePt,
        Optional<BigDecimal> exactLineHeightPt,
        Optional<ParagraphLayoutOverride> layoutOverride,
        boolean keepWithNext,
        boolean keepLines
) implements DocxBlock {

    public DocxParagraph(String text, StyleRule styleRule) {
        this(text, styleRule, Optional.empty(), Optional.empty(), Optional.empty(), false, false);
    }

    public DocxParagraph(String text, StyleRule styleRule, Optional<BigDecimal> spacingBeforeOverridePt) {
        this(text, styleRule, spacingBeforeOverridePt, Optional.empty(), Optional.empty(), false, false);
    }

    public DocxParagraph(
            String text,
            StyleRule styleRule,
            Optional<BigDecimal> spacingBeforeOverridePt,
            Optional<BigDecimal> exactLineHeightPt
    ) {
        this(text, styleRule, spacingBeforeOverridePt, exactLineHeightPt, Optional.empty(), false, false);
    }

    public DocxParagraph(
            String text,
            StyleRule styleRule,
            Optional<BigDecimal> spacingBeforeOverridePt,
            Optional<BigDecimal> exactLineHeightPt,
            Optional<ParagraphLayoutOverride> layoutOverride
    ) {
        this(text, styleRule, spacingBeforeOverridePt, exactLineHeightPt, layoutOverride, false, false);
    }

    public DocxParagraph {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text must not be blank.");
        }

        Objects.requireNonNull(styleRule, "styleRule must not be null");
        Objects.requireNonNull(spacingBeforeOverridePt, "spacingBeforeOverridePt must not be null");
        Objects.requireNonNull(exactLineHeightPt, "exactLineHeightPt must not be null");
        Objects.requireNonNull(layoutOverride, "layoutOverride must not be null");

        spacingBeforeOverridePt.ifPresent(value -> {
            if (value.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("spacingBeforeOverridePt must not be negative.");
            }
        });

        exactLineHeightPt.ifPresent(value -> {
            if (value.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("exactLineHeightPt must be greater than zero.");
            }
        });
    }
}
