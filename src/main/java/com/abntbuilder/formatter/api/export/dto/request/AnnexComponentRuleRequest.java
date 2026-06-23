package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.annex.AnnexComponentRule;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AnnexComponentRuleRequest(
        @NotBlank String componentId,
        @NotBlank String headingTemplate,
        @NotBlank String headingStyleId,
        @NotBlank String paragraphStyleId,
        @NotEmpty List<String> sectionTitleStyleIdsByLevel
) {
    public AnnexComponentRule toDomain() {
        return new AnnexComponentRule(componentId, headingTemplate, headingStyleId,
                paragraphStyleId, sectionTitleStyleIdsByLevel);
    }
}
