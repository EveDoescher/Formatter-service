package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.bodycontent.BodyContentComponent;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BodyContentRequest(
        @Valid @NotEmpty List<BodySectionRequest> sections
) {

    public BodyContentComponent toDomain() {
        return new BodyContentComponent(
                sections == null
                        ? List.of()
                        : sections.stream()
                        .map(BodySectionRequest::toDomain)
                        .toList()
        );
    }
}
