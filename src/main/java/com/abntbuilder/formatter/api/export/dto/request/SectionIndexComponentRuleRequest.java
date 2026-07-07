package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.sectionindex.SectionIndexComponentRule;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SectionIndexComponentRuleRequest(
        @NotBlank String headingStyleId,
        @NotBlank String headingText,
        @NotEmpty List<String> entryStyleIdsByLevel,
        boolean useTocField
) {
    public SectionIndexComponentRule toDomain(String componentId) {
        return new SectionIndexComponentRule(componentId, headingStyleId, headingText,
                entryStyleIdsByLevel, useTocField);
    }
}
