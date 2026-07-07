package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyTableColumn;

public record BodyTableColumnRequest(
        String header
) {

    BodyTableColumn toDomain() {
        return new BodyTableColumn(header);
    }
}
