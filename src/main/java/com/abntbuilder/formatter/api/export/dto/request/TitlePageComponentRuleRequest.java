package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.titlepage.TitlePageComponentRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TitlePageComponentRuleRequest(
        @NotBlank String componentId,

        @Valid
        @NotNull
        TitlePageStyleMappingRequest styleMapping,

        @Valid
        @NotNull
        TitlePageTextTemplateRuleRequest textTemplates,

        @Valid
        @NotNull
        SinglePageLayoutRuleRequest layoutRule
) {

    public TitlePageComponentRule toDomain() {
        return new TitlePageComponentRule(
                componentId,
                styleMapping.toDomain(),
                textTemplates.toDomain(),
                layoutRule.toDomain()
        );
    }
}
