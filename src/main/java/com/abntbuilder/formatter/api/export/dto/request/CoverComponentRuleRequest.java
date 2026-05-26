package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.cover.CoverComponentRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CoverComponentRuleRequest(
        @NotBlank String componentId,

        @Valid
        @NotNull
        CoverStyleMappingRequest styleMapping,

        @Valid
        @NotNull
        CoverLayoutRuleRequest layoutRule
) {
    public CoverComponentRule toDomain() {
        return new CoverComponentRule(
                componentId,
                styleMapping.toDomain(),
                layoutRule.toDomain()
        );
    }
}