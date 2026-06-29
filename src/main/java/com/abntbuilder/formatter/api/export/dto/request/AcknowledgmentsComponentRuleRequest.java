package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.acknowledgments.AcknowledgmentsComponentRule;
import jakarta.validation.constraints.NotBlank;

public record AcknowledgmentsComponentRuleRequest(
        @NotBlank String componentId,
        @NotBlank String headingStyleId,
        @NotBlank String headingText,
        @NotBlank String textStyleId,
        int blankLinesAfterHeading
) {
    public AcknowledgmentsComponentRule toDomain() {
        return new AcknowledgmentsComponentRule(componentId, headingStyleId, headingText, textStyleId, blankLinesAfterHeading);
    }
}
