package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.content.sectioned.SectionedItem;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.CitationFormattingRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record SectionedItemRequest(
        @NotBlank String title,
        @Valid List<BodySectionRequest> sections
) {
    public SectionedItem toDomain(CitationFormattingRule citationFormatting) {
        return new SectionedItem(
                title,
                sections == null ? List.of() : sections.stream().map(s -> s.toDomain(citationFormatting)).toList()
        );
    }
}
