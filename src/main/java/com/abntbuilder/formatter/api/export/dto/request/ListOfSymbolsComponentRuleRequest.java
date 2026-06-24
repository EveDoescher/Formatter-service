package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.listofsymbols.ListOfSymbolsComponentRule;
import jakarta.validation.constraints.NotBlank;

public record ListOfSymbolsComponentRuleRequest(
        @NotBlank String headingStyleId,
        @NotBlank String headingText,
        @NotBlank String entryStyleId,
        @NotBlank String termSeparator
) {
    public ListOfSymbolsComponentRule toDomain(String componentId) {
        return new ListOfSymbolsComponentRule(componentId, headingStyleId, headingText,
                entryStyleId, termSeparator);
    }
}
