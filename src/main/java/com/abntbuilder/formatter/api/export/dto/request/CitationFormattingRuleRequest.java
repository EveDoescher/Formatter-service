package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.bodycontent.CitationFormattingRule;
import jakarta.validation.constraints.NotBlank;

public record CitationFormattingRuleRequest(
        @NotBlank String pagePrefix,
        @NotBlank String multiAuthorJoiner,
        @NotBlank String etAl,
        @NotBlank String apudConnector,
        @NotBlank String suppressionMarker,
        @NotBlank String emphasisOursLabel,
        @NotBlank String emphasisAuthorLabel
) {

    public CitationFormattingRule toDomain() {
        return new CitationFormattingRule(
                pagePrefix,
                multiAuthorJoiner,
                etAl,
                apudConnector,
                suppressionMarker,
                emphasisOursLabel,
                emphasisAuthorLabel
        );
    }
}
