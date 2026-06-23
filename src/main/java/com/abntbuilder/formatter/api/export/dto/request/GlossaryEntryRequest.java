package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.glossary.GlossaryEntry;
import jakarta.validation.constraints.NotBlank;

public record GlossaryEntryRequest(
        @NotBlank String term,
        @NotBlank String definition
) {
    public GlossaryEntry toDomain() {
        return new GlossaryEntry(term, definition);
    }
}
