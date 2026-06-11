package com.abntbuilder.formatter.output.docx.api;

import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.TextAlignment;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public record DocxTableBlock(
        List<String> headers,
        List<List<String>> rows,
        StyleRule headerStyleRule,
        StyleRule cellStyleRule,
        BigDecimal widthPercent,
        TextAlignment alignment,
        boolean repeatHeaderOnPageBreak,
        boolean keepWithNext,
        boolean keepLines
) implements DocxBlock {

    public DocxTableBlock {
        requireNonEmpty(headers, "headers");
        requireNonEmpty(rows, "rows");
        Objects.requireNonNull(headerStyleRule, "headerStyleRule must not be null");
        Objects.requireNonNull(cellStyleRule, "cellStyleRule must not be null");
        requirePositive(widthPercent, "widthPercent");
        Objects.requireNonNull(alignment, "alignment must not be null");

        List<String> resolvedHeaders = List.copyOf(headers);
        List<List<String>> resolvedRows = rows.stream()
                .map(row -> {
                    requireNonEmpty(row, "row");

                    if (row.size() != resolvedHeaders.size()) {
                        throw new IllegalArgumentException("table row cell count must match header count.");
                    }

                    for (String cell : row) {
                        Objects.requireNonNull(cell, "table cell must not be null");
                    }

                    return List.copyOf(row);
                })
                .toList();

        headers = resolvedHeaders;
        rows = resolvedRows;
    }

    private static void requireNonEmpty(List<?> value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");

        if (value.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be empty.");
        }
    }

    private static void requirePositive(BigDecimal value, String fieldName) {
        if (value == null || value.signum() <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than zero.");
        }
    }
}
