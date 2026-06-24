package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.listoffigures.ListOfFiguresComponent;

public record ListOfFiguresRequest() {
    public ListOfFiguresComponent toDomain() { return new ListOfFiguresComponent(); }
}
