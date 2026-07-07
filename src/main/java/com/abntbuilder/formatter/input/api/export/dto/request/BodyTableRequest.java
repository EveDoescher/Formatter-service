package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyTable;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

public record BodyTableRequest(
        String id,
        String continuationGroupId,
        String caption,
        String source,
        @Valid List<BodyTableColumnRequest> columns,
        @Valid List<BodyTableRowRequest> rows
) {

    BodyTable toDomain() {
        return new BodyTable(
                id,
                Optional.ofNullable(continuationGroupId),
                caption,
                Optional.ofNullable(source),
                columns == null ? null : columns.stream().map(BodyTableColumnRequest::toDomain).toList(),
                rows == null ? null : rows.stream().map(BodyTableRowRequest::toDomain).toList()
        );
    }
}
