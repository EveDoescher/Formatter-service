package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.listoftables.ListOfTablesComponent;

public record ListOfTablesRequest() {
    public ListOfTablesComponent toDomain() { return new ListOfTablesComponent(); }
}
