package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.content.DocumentComponent;
import com.abntbuilder.formatter.engine.model.content.sectionindex.SectionIndexContent;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.CitationFormattingRule;

public final class SectionIndexContentRequest implements ComponentContentRequest {

    @Override
    public DocumentComponent toDomain(String componentId, CitationFormattingRule citationFormatting) {
        return new SectionIndexContent(componentId);
    }
}
