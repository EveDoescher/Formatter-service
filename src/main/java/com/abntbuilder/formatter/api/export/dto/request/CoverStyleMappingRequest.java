package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.cover.CoverStyleMapping;
import jakarta.validation.constraints.NotBlank;

public record CoverStyleMappingRequest(
        @NotBlank
        String institutionalLinesStyleId,
        @NotBlank
        String authorsStyleId,
        @NotBlank String titleStyleId,
        @NotBlank String subtitleStyleId,
        @NotBlank
        String cityStyleId,
        @NotBlank
        String yearStyleId
) {
    public CoverStyleMapping toDomain() {
        return new CoverStyleMapping(
                institutionalLinesStyleId,
                authorsStyleId,
                titleStyleId,
                subtitleStyleId,
                cityStyleId,
                yearStyleId
        );
    }
}
