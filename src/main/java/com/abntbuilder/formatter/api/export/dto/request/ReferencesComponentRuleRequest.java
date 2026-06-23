package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.references.ReferencesComponentRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReferencesComponentRuleRequest(
        @NotBlank String componentId,
        @NotBlank String headingStyleId,
        @NotBlank String headingText,
        @NotBlank String entryStyleId,
        @Min(0) int blankLinesBetweenEntries,
        @NotNull @Valid ReferencesFormattingRuleRequest formattingRule
) {
    public ReferencesComponentRule toDomain() {
        return new ReferencesComponentRule(
                componentId, headingStyleId, headingText, entryStyleId,
                blankLinesBetweenEntries, formattingRule.toDomain()
        );
    }
}
