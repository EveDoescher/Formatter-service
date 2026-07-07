package com.abntbuilder.formatter.engine.model.output;

import java.util.Objects;

public record DocxTableCell(
        String text,
        int colspan,
        boolean rowspanStart,
        boolean rowspanContinuation
) {
    public DocxTableCell {
        Objects.requireNonNull(text, "text must not be null");
        if (colspan < 1) throw new IllegalArgumentException("colspan must be >= 1.");
        if (rowspanStart && rowspanContinuation)
            throw new IllegalArgumentException("rowspanStart and rowspanContinuation cannot both be true.");
    }

    public DocxTableCell(String text) {
        this(text, 1, false, false);
    }
}
