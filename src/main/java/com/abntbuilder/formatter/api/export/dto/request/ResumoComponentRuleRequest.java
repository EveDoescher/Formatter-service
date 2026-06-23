package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.resumo.ResumoComponentRule;
import jakarta.validation.constraints.NotBlank;

public record ResumoComponentRuleRequest(
        @NotBlank String componentId,
        @NotBlank String headingStyleId,
        @NotBlank String headingText,
        @NotBlank String textStyleId,
        @NotBlank String keywordsStyleId,
        @NotBlank String keywordsLabel,
        @NotBlank String keywordsSeparator
) {
    public ResumoComponentRule toDomain() {
        return new ResumoComponentRule(componentId, headingStyleId, headingText,
                textStyleId, keywordsStyleId, keywordsLabel, keywordsSeparator);
    }
}
