package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.profile.component.elementindex.ElementIndexComponentRule;
import com.abntbuilder.formatter.engine.model.profile.component.elementindex.ElementType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ElementIndexComponentRuleRequest(
        @NotNull ElementType elementType,
        @NotBlank String headingStyleId,
        @NotBlank String headingText,
        @NotBlank String entryStyleId,
        @NotBlank String entryTemplate,
        int blankLinesAfterHeading
) {
    public ElementIndexComponentRule toDomain(String componentId) {
        return new ElementIndexComponentRule(componentId, elementType, headingStyleId,
                headingText, entryStyleId, entryTemplate, blankLinesAfterHeading);
    }
}
