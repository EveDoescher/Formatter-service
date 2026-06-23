package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.annex.AnnexComponent;
import com.abntbuilder.formatter.profile.model.component.bodycontent.CitationFormattingRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AnnexRequest(
        @NotEmpty @Valid List<AnnexItemRequest> items
) {
    public AnnexComponent toDomain(CitationFormattingRule citationFormatting) {
        return new AnnexComponent(items.stream().map(i -> i.toDomain(citationFormatting)).toList());
    }
}
