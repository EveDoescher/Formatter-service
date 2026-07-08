package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.profile.component.sectionindex.SectionIndexComponentRule;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SectionIndexComponentRuleRequest(
        @NotBlank String headingStyleId,
        @NotBlank String headingText,
        @NotEmpty List<String> entryStyleIdsByLevel,
        boolean useTocField,
        int blankLinesAfterHeading
) {
    public SectionIndexComponentRule toDomain(String componentId) {
        return new SectionIndexComponentRule(componentId, headingStyleId, headingText,
                entryStyleIdsByLevel, useTocField, blankLinesAfterHeading);
    }
}
