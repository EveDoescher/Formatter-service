package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.errata.ErrataComponentRule;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ErrataComponentRuleRequest(
        @NotBlank String componentId,
        @NotBlank String headingStyleId,
        @NotBlank String headingText,
        @NotBlank String tableHeaderStyleId,
        @NotBlank String tableCellStyleId,
        @NotEmpty List<@NotBlank String> tableHeaders,
        int blankLinesAfterHeading
) {
    public ErrataComponentRule toDomain() {
        return new ErrataComponentRule(componentId, headingStyleId, headingText,
                tableHeaderStyleId, tableCellStyleId, tableHeaders, blankLinesAfterHeading);
    }
}
