package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.sectioned.SectionedComponentRule;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SectionedComponentRuleRequest(
        @NotBlank String componentId,
        @NotBlank String headingTemplate,
        @NotBlank String headingStyleId,
        @NotBlank String paragraphStyleId,
        @NotEmpty List<String> sectionTitleStyleIdsByLevel
) {
    public SectionedComponentRule toDomain() {
        return new SectionedComponentRule(componentId, headingTemplate, headingStyleId,
                paragraphStyleId, sectionTitleStyleIdsByLevel);
    }
}
