package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.ComponentContentBindings;
import com.abntbuilder.formatter.profile.model.component.titlepage.TitlePageComponentRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record TitlePageComponentRuleRequest(
        @NotBlank String componentId,
        Map<@NotBlank String, @NotBlank String> contentBindings,

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
                new ComponentContentBindings(contentBindings == null ? Map.of() : contentBindings),
                styleMapping.toDomain(),
                textTemplates.toDomain(),
                layoutRule.toDomain()
        );
    }
}
