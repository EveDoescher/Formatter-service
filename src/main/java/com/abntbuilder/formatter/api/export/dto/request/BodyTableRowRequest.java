package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.bodycontent.BodyTableRow;

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
