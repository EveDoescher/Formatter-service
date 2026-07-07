package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.profile.component.sectioned.SectionedComponentRule;
import com.abntbuilder.formatter.engine.model.profile.component.sectioned.SectionedComponentRule.IndexingStyle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SectionedComponentRuleRequest(
        @NotBlank String componentId,
        @NotBlank String headingTemplate,
        @NotBlank String headingStyleId,
        @NotBlank String paragraphStyleId,
        @NotEmpty List<String> sectionTitleStyleIdsByLevel,
        IndexingStyle indexingStyle
) {
    public SectionedComponentRule toDomain() {
        return new SectionedComponentRule(componentId, headingTemplate, headingStyleId,
                paragraphStyleId, sectionTitleStyleIdsByLevel,
                indexingStyle != null ? indexingStyle : IndexingStyle.ALPHABETIC);
    }
}
