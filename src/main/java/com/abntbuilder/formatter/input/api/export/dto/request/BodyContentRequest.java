package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyContentComponent;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.CitationFormattingRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BodyContentRequest(
        @Valid @NotEmpty List<BodySectionRequest> sections
) {

    public BodyContentComponent toDomain(String componentId, CitationFormattingRule citationFormatting) {
        return new BodyContentComponent(
                componentId,
                sections.stream()
                        .map(s -> s.toDomain(citationFormatting))
                        .toList()
        );
    }

    public BodyContentComponent toDomain(String componentId) {
        return toDomain(componentId, null);
    }

    public BodyContentComponent toDomain(CitationFormattingRule citationFormatting) {
        return toDomain("bodyContent", citationFormatting);
    }

    public BodyContentComponent toDomain() {
        return toDomain("bodyContent", null);
    }
}
