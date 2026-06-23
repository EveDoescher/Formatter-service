package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.bodycontent.CrossReferenceLabelsRule;
import jakarta.validation.constraints.NotBlank;

public record CrossReferenceLabelsRuleRequest(
        @NotBlank String sectionLabel,
        @NotBlank String figureLabel,
        @NotBlank String tableLabel,
        @NotBlank String frameLabel,
        @NotBlank String chartLabel,
        @NotBlank String codeListingLabel,
        @NotBlank String equationLabel
) {
    public CrossReferenceLabelsRule toDomain() {
        return new CrossReferenceLabelsRule(
                sectionLabel,
                figureLabel,
                tableLabel,
                frameLabel,
                chartLabel,
                codeListingLabel,
                equationLabel
        );
    }
}
