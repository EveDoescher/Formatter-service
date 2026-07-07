package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.sectioned.SectionedContent;
import com.abntbuilder.formatter.document.component.sectioned.SectionedItem;
import com.abntbuilder.formatter.profile.model.component.bodycontent.CitationFormattingRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AppendixRequest(
        @NotEmpty @Valid List<SectionedItemRequest> items
) {
    public SectionedContent toDomain(CitationFormattingRule citationFormatting) {
        List<SectionedItem> domainItems = items.stream()
                .map(i -> i.toDomain(citationFormatting))
                .toList();
        return new SectionedContent("appendix", domainItems);
    }
}
