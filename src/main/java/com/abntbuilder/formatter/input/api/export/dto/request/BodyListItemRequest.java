package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyListItem;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.CitationFormattingRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record BodyListItemRequest(
        @Valid @NotEmpty List<BodyInlineRequest> content,
        @Valid BodyListRequest subList
) {

    public BodyListItem toDomain(CitationFormattingRule citationFormatting) {
        return new BodyListItem(
                content.stream()
                        .map(inline -> inline.toDomain(citationFormatting))
                        .toList(),
                java.util.Optional.ofNullable(subList == null ? null : subList.toDomain(citationFormatting))
        );
    }
}
