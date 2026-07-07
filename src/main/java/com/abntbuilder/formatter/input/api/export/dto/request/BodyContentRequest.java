package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.content.DocumentComponent;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyContentComponent;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.CitationFormattingRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BodyContentRequest(
        @Valid @NotEmpty List<BodySectionRequest> sections
) implements ComponentContentRequest {

    @Override
    public DocumentComponent toDomain(String componentId, CitationFormattingRule citationFormatting) {
        return new BodyContentComponent(
                componentId,
                sections.stream()
                        .map(s -> s.toDomain(citationFormatting))
                        .toList()
        );
    }

    public BodyContentComponent toBodyContent(String componentId) {
        return (BodyContentComponent) toDomain(componentId, null);
    }

    public BodyContentComponent toBodyContent(CitationFormattingRule citationFormatting) {
        return (BodyContentComponent) toDomain("bodyContent", citationFormatting);
    }

    public BodyContentComponent toBodyContent() {
        return (BodyContentComponent) toDomain("bodyContent", null);
    }
}
