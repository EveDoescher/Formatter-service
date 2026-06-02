package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.titlepage.TitlePageTextTemplateRule;
import jakarta.validation.constraints.NotBlank;

public record TitlePageTextTemplateRuleRequest(
        @NotBlank String natureTemplate,
        @NotBlank String advisorTemplate,
        @NotBlank String coadvisorTemplate
) {

    public TitlePageTextTemplateRule toDomain() {
        return new TitlePageTextTemplateRule(
                natureTemplate,
                advisorTemplate,
                coadvisorTemplate
        );
    }
}
