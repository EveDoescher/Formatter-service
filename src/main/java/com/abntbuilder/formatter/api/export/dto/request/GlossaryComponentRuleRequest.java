package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.glossary.GlossaryComponentRule;
import jakarta.validation.constraints.NotBlank;

public record GlossaryComponentRuleRequest(
        @NotBlank String componentId,
        @NotBlank String headingStyleId,
        @NotBlank String headingText,
        @NotBlank String entryStyleId,
        @NotBlank String termSeparator
) {
    public GlossaryComponentRule toDomain() {
        return new GlossaryComponentRule(componentId, headingStyleId, headingText, entryStyleId, termSeparator);
    }
}
