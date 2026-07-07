package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyFrame;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Optional;

public record BodyFrameRequest(
        @NotBlank String id,
        String continuationGroupId,
        @NotBlank String caption,
        String source,
        @jakarta.validation.Valid @jakarta.validation.constraints.NotEmpty List<BodyTableColumnRequest> columns,
        @jakarta.validation.Valid @jakarta.validation.constraints.NotEmpty List<BodyTableRowRequest> rows
) {
    public BodyFrame toDomain() {
        return new BodyFrame(
                id,
                Optional.ofNullable(continuationGroupId),
                caption,
                Optional.ofNullable(source),
                columns.stream().map(BodyTableColumnRequest::toDomain).toList(),
                rows.stream().map(BodyTableRowRequest::toDomain).toList()
        );
    }
}
