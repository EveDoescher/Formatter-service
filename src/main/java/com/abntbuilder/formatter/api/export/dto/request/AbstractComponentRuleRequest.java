package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.abstracten.AbstractComponentRule;
import jakarta.validation.constraints.NotBlank;

public record AbstractComponentRuleRequest(
        @NotBlank String componentId,
        @NotBlank String headingStyleId,
        @NotBlank String textStyleId,
        @NotBlank String keywordsStyleId,
        @NotBlank String keywordsSeparator,
        @NotBlank String keywordsTerminator,
        int blankLinesAfterHeading
) {
    public AbstractComponentRule toDomain() {
        return new AbstractComponentRule(componentId, headingStyleId,
                textStyleId, keywordsStyleId, keywordsSeparator, keywordsTerminator, blankLinesAfterHeading);
    }
}
