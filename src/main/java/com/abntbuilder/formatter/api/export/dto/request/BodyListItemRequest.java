package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.bodycontent.BodyListItem;
import com.abntbuilder.formatter.profile.model.component.bodycontent.CitationFormattingRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record BodyListItemRequest(@Valid @NotEmpty List<BodyInlineRequest> content) {

    public BodyListItem toDomain(CitationFormattingRule citationFormatting) {
        return new BodyListItem(
                content.stream()
                        .map(inline -> inline.toDomain(citationFormatting))
                        .toList()
        );
    }
}
