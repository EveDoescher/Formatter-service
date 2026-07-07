package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.elementindex.ElementIndexContent;

public record ListOfTablesRequest() {
    public ElementIndexContent toDomain() { return new ElementIndexContent("listOfTables"); }
}
