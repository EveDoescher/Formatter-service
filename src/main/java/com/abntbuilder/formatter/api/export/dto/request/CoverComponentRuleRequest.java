package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.ComponentContentBindings;
import com.abntbuilder.formatter.profile.model.component.cover.CoverComponentRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record CoverComponentRuleRequest(
        @NotBlank String componentId,
        Map<@NotBlank String, @NotBlank String> contentBindings,

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
                new ComponentContentBindings(contentBindings == null ? Map.of() : contentBindings),
                styleMapping.toDomain(),
                layoutRule.toDomain()
        );
    }
}
