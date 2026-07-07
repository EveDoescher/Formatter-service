package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.content.sectionindex.SectionIndexContent;

public record SummaryRequest() {
    public SectionIndexContent toDomain() { return new SectionIndexContent("summary"); }
}
