package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.abstracten.AbstractComponent;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AbstractRequest(
        @Valid @NotEmpty List<AbstractEntryRequest> entries
) {
    public AbstractComponent toDomain() {
        return new AbstractComponent(
                entries.stream().map(AbstractEntryRequest::toDomain).toList()
        );
    }
}
