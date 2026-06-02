package com.abntbuilder.formatter.profile.model.component.titlepage;

import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;

public record TitlePageTextTemplateRule(
        String natureTemplate,
        String advisorTemplate,
        String coadvisorTemplate
) {

    public TitlePageTextTemplateRule {
        requireNonBlank(natureTemplate, "natureTemplate");
        requireNonBlank(advisorTemplate, "advisorTemplate");
        requireNonBlank(coadvisorTemplate, "coadvisorTemplate");
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new InvalidProfileStructureException(fieldName + " must not be blank.");
        }
    }
}
