package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyList;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyListType;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.CitationFormattingRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record BodyListRequest(
        @NotNull BodyListType type,
        @Valid @NotEmpty List<BodyListItemRequest> items
) {
    public BodyList toDomain(CitationFormattingRule citationFormatting) {
        return new BodyList(type,
                items.stream()
                        .map(item -> item.toDomain(citationFormatting))
                        .toList());
    }
}
