package com.abntbuilder.formatter.document.component.bodycontent;

import java.util.Objects;

public record BodyTableCell(
        String text,
        int colspan,
        boolean rowspanStart,
        boolean rowspanContinuation
) {
    public BodyTableCell {
        Objects.requireNonNull(text, "text must not be null");
        if (colspan < 1) throw new IllegalArgumentException("colspan must be >= 1.");
        if (rowspanStart && rowspanContinuation)
            throw new IllegalArgumentException("rowspanStart and rowspanContinuation cannot both be true.");
    }

    public BodyTableCell(String text) {
        this(text, 1, false, false);
    }
}
