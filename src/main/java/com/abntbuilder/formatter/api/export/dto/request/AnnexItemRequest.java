package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.annex.AnnexItem;
import com.abntbuilder.formatter.profile.model.component.bodycontent.CitationFormattingRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record AnnexItemRequest(
        @NotBlank String title,
        @Valid List<BodySectionRequest> sections
) {
    public AnnexItem toDomain(CitationFormattingRule citationFormatting) {
        return new AnnexItem(
                title,
                sections == null ? List.of() : sections.stream().map(s -> s.toDomain(citationFormatting)).toList()
        );
    }
}
