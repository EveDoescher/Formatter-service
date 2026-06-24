package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.listofsymbols.ListOfSymbolsComponent;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ListOfSymbolsRequest(@NotEmpty @Valid List<SymbolEntryRequest> entries) {
    public ListOfSymbolsComponent toDomain() {
        return new ListOfSymbolsComponent(entries.stream().map(SymbolEntryRequest::toDomain).toList());
    }
}
