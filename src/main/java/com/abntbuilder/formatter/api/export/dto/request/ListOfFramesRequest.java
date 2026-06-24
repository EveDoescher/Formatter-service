package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.listofframes.ListOfFramesComponent;

public record ListOfFramesRequest() {
    public ListOfFramesComponent toDomain() { return new ListOfFramesComponent(); }
}
