package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.listofcharts.ListOfChartsComponent;

public record ListOfChartsRequest() {
    public ListOfChartsComponent toDomain() { return new ListOfChartsComponent(); }
}
