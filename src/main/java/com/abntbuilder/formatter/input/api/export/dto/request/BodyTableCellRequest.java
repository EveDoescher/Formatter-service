package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyTableCell;

public record BodyTableCellRequest(
        String text,
        int colspan,
        boolean rowspanStart,
        boolean rowspanContinuation
) {
    public BodyTableCellRequest(String text) {
        this(text, 1, false, false);
    }

    public BodyTableCell toDomain() {
        return new BodyTableCell(text, colspan < 1 ? 1 : colspan, rowspanStart, rowspanContinuation);
    }
}
