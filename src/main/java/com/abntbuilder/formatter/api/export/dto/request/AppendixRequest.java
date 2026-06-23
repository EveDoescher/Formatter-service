package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.appendix.AppendixComponent;
import com.abntbuilder.formatter.profile.model.component.bodycontent.CitationFormattingRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AppendixRequest(
        @NotEmpty @Valid List<AppendixItemRequest> items
) {
    public AppendixComponent toDomain(CitationFormattingRule citationFormatting) {
        return new AppendixComponent(items.stream().map(i -> i.toDomain(citationFormatting)).toList());
    }
}
