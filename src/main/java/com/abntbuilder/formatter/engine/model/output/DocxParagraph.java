package com.abntbuilder.formatter.engine.model.output;

import com.abntbuilder.formatter.engine.model.profile.StyleRule;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record DocxParagraph(
        List<DocxRun> runs,
        StyleRule styleRule,
        Optional<BigDecimal> spacingBeforeOverridePt,
        Optional<BigDecimal> exactLineHeightPt,
        Optional<ParagraphLayoutOverride> layoutOverride,
        boolean keepWithNext,
        boolean keepLines
) implements DocxBlock {

    public DocxParagraph(List<DocxRun> runs, StyleRule styleRule) {
        this(runs, styleRule, Optional.empty(), Optional.empty(), Optional.empty(), false, false);
    }

    public DocxParagraph(List<DocxRun> runs, StyleRule styleRule, Optional<BigDecimal> spacingBeforeOverridePt) {
        this(runs, styleRule, spacingBeforeOverridePt, Optional.empty(), Optional.empty(), false, false);
    }

    public DocxParagraph(
            List<DocxRun> runs,
            StyleRule styleRule,
            Optional<BigDecimal> spacingBeforeOverridePt,
            Optional<BigDecimal> exactLineHeightPt
    ) {
        this(runs, styleRule, spacingBeforeOverridePt, exactLineHeightPt, Optional.empty(), false, false);
    }

    public DocxParagraph(
            List<DocxRun> runs,
            StyleRule styleRule,
            Optional<BigDecimal> spacingBeforeOverridePt,
            Optional<BigDecimal> exactLineHeightPt,
            Optional<ParagraphLayoutOverride> layoutOverride
    ) {
        this(runs, styleRule, spacingBeforeOverridePt, exactLineHeightPt, layoutOverride, false, false);
    }

    public DocxParagraph {
        Objects.requireNonNull(runs, "runs must not be null");
        if (runs.isEmpty()) {
            throw new IllegalArgumentException("runs must not be empty.");
        }
        runs = List.copyOf(runs);
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
