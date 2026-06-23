package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.references.ReferencesComponent;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ReferencesRequest(
        @NotEmpty @Valid List<ReferenceEntryRequest> entries
) {
    public ReferencesComponent toDomain() {
        return new ReferencesComponent(entries.stream().map(ReferenceEntryRequest::toDomain).toList());
    }
}
