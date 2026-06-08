package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.titlepage.TitlePageNature;
import jakarta.validation.constraints.NotBlank;

public record TitlePageNatureRequest(
        @NotBlank String workType,
        @NotBlank String degreeObjective,
        @NotBlank String courseName,
        @NotBlank String institutionName
) {

    public TitlePageNature toDomain() {
        return new TitlePageNature(
                workType,
                degreeObjective,
                courseName,
                institutionName
        );
    }
}
