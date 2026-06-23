package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.epigraph.EpigraphComponentRule;
import jakarta.validation.constraints.NotBlank;

public record EpigraphComponentRuleRequest(
        @NotBlank String componentId,
        @NotBlank String textStyleId,
        @NotBlank String authorStyleId,
        @NotBlank String authorTemplate
) {
    public EpigraphComponentRule toDomain() {
        return new EpigraphComponentRule(componentId, textStyleId, authorStyleId, authorTemplate);
    }
}
