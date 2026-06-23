package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.appendix.AppendixComponentRule;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AppendixComponentRuleRequest(
        @NotBlank String componentId,
        @NotBlank String headingTemplate,
        @NotBlank String headingStyleId,
        @NotBlank String paragraphStyleId,
        @NotEmpty List<String> sectionTitleStyleIdsByLevel
) {
    public AppendixComponentRule toDomain() {
        return new AppendixComponentRule(componentId, headingTemplate, headingStyleId,
                paragraphStyleId, sectionTitleStyleIdsByLevel);
    }
}
