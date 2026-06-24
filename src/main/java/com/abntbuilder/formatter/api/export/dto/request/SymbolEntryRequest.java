package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.listofsymbols.SymbolEntry;
import jakarta.validation.constraints.NotBlank;

public record SymbolEntryRequest(@NotBlank String symbol, @NotBlank String meaning) {
    public SymbolEntry toDomain() { return new SymbolEntry(symbol, meaning); }
}
