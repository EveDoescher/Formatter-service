package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.content.sectioned.SectionedContent;
import com.abntbuilder.formatter.engine.model.content.sectioned.SectionedItem;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.CitationFormattingRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AnnexRequest(
        @NotEmpty @Valid List<SectionedItemRequest> items
) {
    public SectionedContent toDomain(CitationFormattingRule citationFormatting) {
        List<SectionedItem> domainItems = items.stream()
                .map(i -> i.toDomain(citationFormatting))
                .toList();
        return new SectionedContent("annex", domainItems);
    }
}
