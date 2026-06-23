package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.glossary.GlossaryComponent;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record GlossaryRequest(
        @NotEmpty @Valid List<GlossaryEntryRequest> entries
) {
    public GlossaryComponent toDomain() {
        return new GlossaryComponent(entries.stream().map(GlossaryEntryRequest::toDomain).toList());
    }
}
