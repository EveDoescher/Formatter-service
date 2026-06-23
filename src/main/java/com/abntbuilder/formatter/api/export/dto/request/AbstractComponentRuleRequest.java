package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.abstracten.AbstractComponentRule;
import jakarta.validation.constraints.NotBlank;

public record AbstractComponentRuleRequest(
        @NotBlank String componentId,
        @NotBlank String headingStyleId,
        @NotBlank String headingText,
        @NotBlank String textStyleId,
        @NotBlank String keywordsStyleId,
        @NotBlank String keywordsLabel,
        @NotBlank String keywordsSeparator
) {
    public AbstractComponentRule toDomain() {
        return new AbstractComponentRule(componentId, headingStyleId, headingText,
                textStyleId, keywordsStyleId, keywordsLabel, keywordsSeparator);
    }
}
