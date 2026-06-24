package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.summary.SummaryComponent;

public record SummaryRequest() {
    public SummaryComponent toDomain() { return new SummaryComponent(); }
}
