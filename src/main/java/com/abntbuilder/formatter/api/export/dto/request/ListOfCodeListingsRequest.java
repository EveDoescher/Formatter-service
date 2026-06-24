package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.listofcodelistings.ListOfCodeListingsComponent;

public record ListOfCodeListingsRequest() {
    public ListOfCodeListingsComponent toDomain() { return new ListOfCodeListingsComponent(); }
}
