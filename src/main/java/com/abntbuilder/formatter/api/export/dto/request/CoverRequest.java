package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.cover.CoverComponent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Optional;

public record CoverRequest(
        @NotNull List<@NotBlank String> topLines,
        @NotNull List<@NotBlank String> authorLines,
        @NotBlank String title,
        String subtitle,
        @NotNull List<@NotBlank String> bottomLines
) {
    public CoverComponent toDomain() {
        return new CoverComponent(
                topLines,
                authorLines,
                title,
                subtitle == null ? Optional.empty() : Optional.of(subtitle),
                bottomLines
        );
    }
}