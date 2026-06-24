package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.listofabbreviations.ListOfAbbreviationsComponent;

public record ListOfAbbreviationsRequest() {
    public ListOfAbbreviationsComponent toDomain() { return new ListOfAbbreviationsComponent(); }
}
