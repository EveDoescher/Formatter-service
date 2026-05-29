package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.cover.CoverStyleMapping;
import jakarta.validation.constraints.NotBlank;

public record CoverStyleMappingRequest(
        String institutionalLinesStyleId,
        String authorsStyleId,
        @NotBlank String titleStyleId,
        @NotBlank String subtitleStyleId,
        String cityStyleId,
        String yearStyleId,
        String topLinesStyleId,
        String authorLinesStyleId,
        String bottomLinesStyleId
) {
    public CoverStyleMapping toDomain() {
        if (institutionalLinesStyleId != null || authorsStyleId != null || cityStyleId != null || yearStyleId != null) {
            return new CoverStyleMapping(
                    institutionalLinesStyleId,
                    authorsStyleId,
                    titleStyleId,
                    subtitleStyleId,
                    cityStyleId,
                    yearStyleId
            );
        }

        return new CoverStyleMapping(
                topLinesStyleId,
                authorLinesStyleId,
                titleStyleId,
                subtitleStyleId,
                bottomLinesStyleId
        );
    }
}
