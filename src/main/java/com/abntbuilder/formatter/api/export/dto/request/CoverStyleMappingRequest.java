package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.cover.CoverStyleMapping;
import jakarta.validation.constraints.NotBlank;

public record CoverStyleMappingRequest(
        @NotBlank String topLinesStyleId,
        @NotBlank String authorLinesStyleId,
        @NotBlank String titleStyleId,
        @NotBlank String subtitleStyleId,
        @NotBlank String bottomLinesStyleId
) {
    public CoverStyleMapping toDomain() {
        return new CoverStyleMapping(
                topLinesStyleId,
                authorLinesStyleId,
                titleStyleId,
                subtitleStyleId,
                bottomLinesStyleId
        );
    }
}