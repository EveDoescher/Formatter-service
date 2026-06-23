package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.errata.ErrataComponent;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ErrataRequest(
        @NotEmpty @Valid List<ErrataEntryRequest> entries
) {
    public ErrataComponent toDomain() {
        return new ErrataComponent(entries.stream().map(ErrataEntryRequest::toDomain).toList());
    }
}
