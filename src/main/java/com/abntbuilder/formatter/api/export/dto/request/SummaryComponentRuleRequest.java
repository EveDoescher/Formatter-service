package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.summary.SummaryComponentRule;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SummaryComponentRuleRequest(
        @NotBlank String headingStyleId,
        @NotBlank String headingText,
        @NotEmpty List<String> entryStyleIdsByLevel,
        boolean useTocField
) {
    public SummaryComponentRule toDomain(String componentId) {
        return new SummaryComponentRule(componentId, headingStyleId, headingText,
                entryStyleIdsByLevel, useTocField);
    }
}
