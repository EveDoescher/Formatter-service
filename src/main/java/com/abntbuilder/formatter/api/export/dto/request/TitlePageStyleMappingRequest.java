package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.titlepage.TitlePageStyleMapping;
import jakarta.validation.constraints.NotBlank;

public record TitlePageStyleMappingRequest(
        @NotBlank String authorsStyleId,
        @NotBlank String titleStyleId,
        @NotBlank String subtitleStyleId,
        @NotBlank String natureStyleId,
        @NotBlank String advisorStyleId,
        @NotBlank String coadvisorStyleId,
        @NotBlank String cityStyleId,
        @NotBlank String yearStyleId
) {

    public TitlePageStyleMapping toDomain() {
        return new TitlePageStyleMapping(
                authorsStyleId,
                titleStyleId,
                subtitleStyleId,
                natureStyleId,
                advisorStyleId,
                coadvisorStyleId,
                cityStyleId,
                yearStyleId
        );
    }
}
