package com.abntbuilder.formatter.engine.model.output;

import com.abntbuilder.formatter.engine.model.profile.StyleRule;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record DocxBookmarkParagraph(
        List<DocxRun> runs,
        StyleRule styleRule,
        Optional<BigDecimal> spacingBeforeOverridePt,
        Optional<BigDecimal> exactLineHeightPt,
        Optional<ParagraphLayoutOverride> layoutOverride,
        boolean keepWithNext,
        boolean keepLines,
        String bookmarkName
) implements DocxBlock {

    public DocxBookmarkParagraph {
        Objects.requireNonNull(runs, "runs must not be null");
        if (runs.isEmpty()) throw new IllegalArgumentException("runs must not be empty.");
        runs = List.copyOf(runs);
        Objects.requireNonNull(styleRule, "styleRule must not be null");
        Objects.requireNonNull(spacingBeforeOverridePt, "spacingBeforeOverridePt must not be null");
        Objects.requireNonNull(exactLineHeightPt, "exactLineHeightPt must not be null");
        Objects.requireNonNull(layoutOverride, "layoutOverride must not be null");
        if (bookmarkName == null || bookmarkName.isBlank())
            throw new IllegalArgumentException("bookmarkName must not be blank.");
    }

    public DocxBookmarkParagraph(List<DocxRun> runs, StyleRule styleRule, String bookmarkName) {
        this(runs, styleRule, Optional.empty(), Optional.empty(), Optional.empty(), true, true, bookmarkName);
    }
}
