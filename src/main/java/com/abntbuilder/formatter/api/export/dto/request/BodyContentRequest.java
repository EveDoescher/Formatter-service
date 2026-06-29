package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.bodycontent.BodyContentComponent;
import com.abntbuilder.formatter.profile.model.component.bodycontent.CitationFormattingRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BodyContentRequest(
        @Valid @NotEmpty List<BodySectionRequest> sections
) {

    public BodyContentComponent toDomain(CitationFormattingRule citationFormatting) {
        return new BodyContentComponent(
                sections.stream()
                        .map(s -> s.toDomain(citationFormatting))
                        .toList()
        );
    }

    public BodyContentComponent toDomain() {
        return toDomain(null);
    }
}
