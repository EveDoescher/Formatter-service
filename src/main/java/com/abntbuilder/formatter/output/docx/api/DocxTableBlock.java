package com.abntbuilder.formatter.output.docx.api;

import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.TextAlignment;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public record DocxTableBlock(
        List<String> headers,
        List<List<DocxTableCell>> rows,
        StyleRule headerStyleRule,
        StyleRule cellStyleRule,
        BigDecimal widthPercent,
        TextAlignment alignment,
        boolean repeatHeaderOnPageBreak,
        boolean keepWithNext,
        boolean keepLines,
        TableBorderStyle borderStyle
) implements DocxBlock {

    public DocxTableBlock {
        requireNonEmpty(headers, "headers");
        requireNonEmpty(rows, "rows");
        Objects.requireNonNull(headerStyleRule, "headerStyleRule must not be null");
        Objects.requireNonNull(cellStyleRule, "cellStyleRule must not be null");
        requirePositive(widthPercent, "widthPercent");
        Objects.requireNonNull(alignment, "alignment must not be null");
        Objects.requireNonNull(borderStyle, "borderStyle must not be null");

        List<String> resolvedHeaders = List.copyOf(headers);
        int columnCount = resolvedHeaders.size();
        List<List<DocxTableCell>> resolvedRows = rows.stream()
                .map(row -> {
                    requireNonEmpty(row, "row");
                    int effective = row.stream().mapToInt(DocxTableCell::colspan).sum();
                    if (effective != columnCount) {
                        throw new IllegalArgumentException("table row effective column count must match header count.");
                    }
                    for (DocxTableCell cell : row) {
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
