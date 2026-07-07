package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.content.elementindex.ElementIndexContent;

public record ListOfFiguresRequest() {
    public ElementIndexContent toDomain() { return new ElementIndexContent("listOfFigures"); }
}
