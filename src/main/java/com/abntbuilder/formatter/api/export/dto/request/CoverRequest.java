package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.cover.CoverComponent;

import java.util.List;
import java.util.Optional;

public record CoverRequest(
        List<String> institutionalLines,
        List<String> authors,
        String title,
        String subtitle,
        String city,
        String year,
        List<String> topLines,
        List<String> authorLines,
        List<String> bottomLines
) {

    public CoverComponent toDomain() {
        if (hasSemanticFields()) {
            return new CoverComponent(
                    institutionalLines == null ? List.of() : institutionalLines,
                    authors == null ? List.of() : authors,
                    title,
                    subtitle == null ? Optional.empty() : Optional.of(subtitle),
                    city,
                    year
            );
        }

        return new CoverComponent(
                topLines == null ? List.of() : topLines,
                authorLines == null ? List.of() : authorLines,
                title,
                subtitle == null ? Optional.empty() : Optional.of(subtitle),
                bottomLines == null ? List.of() : bottomLines
        );
    }

    private boolean hasSemanticFields() {
        return institutionalLines != null
                || authors != null
                || city != null
                || year != null;
    }
}
