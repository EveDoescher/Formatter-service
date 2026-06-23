package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.errata.ErrataEntry;
import jakarta.validation.constraints.NotBlank;

public record ErrataEntryRequest(
        @NotBlank String page,
        @NotBlank String line,
        @NotBlank String incorrectText,
        @NotBlank String correctText
) {
    public ErrataEntry toDomain() {
        return new ErrataEntry(page, line, incorrectText, correctText);
    }
}
