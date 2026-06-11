package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.bodycontent.BodyTableColumn;

public record BodyTableColumnRequest(
        String header
) {

    BodyTableColumn toDomain() {
        return new BodyTableColumn(header);
    }
}
