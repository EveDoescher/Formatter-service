package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.indexlist.IndexListComponentRule;
import jakarta.validation.constraints.NotBlank;

public record IndexListComponentRuleRequest(
        @NotBlank String headingStyleId,
        @NotBlank String headingText,
        @NotBlank String entryStyleId,
        @NotBlank String entryTemplate,
        int blankLinesAfterHeading
) {
    public IndexListComponentRule toDomain(String componentId) {
        return new IndexListComponentRule(componentId, headingStyleId, headingText, entryStyleId, entryTemplate, blankLinesAfterHeading);
    }
}
