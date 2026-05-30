package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.cover.CoverComponent;

import java.util.List;
import java.util.Optional;

public record LegacyCoverRequest(
        List<String> topLines,
        List<String> authorLines,
        String title,
        String subtitle,
        List<String> bottomLines
) {

    public CoverComponent toDomain() {
        return new CoverComponent(
                topLines == null ? List.of() : topLines,
                authorLines == null ? List.of() : authorLines,
                title,
                subtitle == null ? Optional.empty() : Optional.of(subtitle),
                bottomLines == null ? List.of() : bottomLines
        );
    }
}
