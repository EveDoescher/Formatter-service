package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyTableRow;

import java.util.List;

public record BodyTableRowRequest(
        List<BodyTableCellRequest> cells
) {

    BodyTableRow toDomain() {
        return new BodyTableRow(
                cells.stream().map(BodyTableCellRequest::toDomain).toList()
        );
    }
}
