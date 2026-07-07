package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.sectionindex.SectionIndexContent;

public record SummaryRequest() {
    public SectionIndexContent toDomain() { return new SectionIndexContent("summary"); }
}
