package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.errata.ErrataComponentRule;
import jakarta.validation.constraints.NotBlank;

public record ErrataComponentRuleRequest(
        @NotBlank String componentId,
        @NotBlank String headingStyleId,
        @NotBlank String headingText,
        @NotBlank String entryStyleId,
        @NotBlank String entryTemplate
) {
    public ErrataComponentRule toDomain() {
        return new ErrataComponentRule(componentId, headingStyleId, headingText, entryStyleId, entryTemplate);
    }
}
