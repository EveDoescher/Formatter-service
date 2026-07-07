package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.elementindex.ElementIndexContent;

public record ListOfFramesRequest() {
    public ElementIndexContent toDomain() { return new ElementIndexContent("listOfFrames"); }
}
