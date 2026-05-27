package com.abntbuilder.formatter.output.docx.api;

import com.abntbuilder.formatter.profile.model.StyleRule;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record DocxMultilineParagraph(
        List<String> lines,
        StyleRule styleRule,
        Optional<BigDecimal> exactLineHeightPt
) implements DocxBlock {

    public DocxMultilineParagraph {
        Objects.requireNonNull(lines, "lines must not be null");
        Objects.requireNonNull(styleRule, "styleRule must not be null");
        Objects.requireNonNull(exactLineHeightPt, "exactLineHeightPt must not be null");

        if (lines.isEmpty()) {
            throw new IllegalArgumentException("lines must not be empty.");
        }

        lines = List.copyOf(lines);

        for (String line : lines) {
            if (line == null || line.isBlank()) {
                throw new IllegalArgumentException("lines must not contain blank values.");
            }
        }

        exactLineHeightPt.ifPresent(value -> {
            if (value.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("exactLineHeightPt must be greater than zero.");
            }
        });
    }
}