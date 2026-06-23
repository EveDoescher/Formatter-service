package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.epigraph.EpigraphComponent;
import jakarta.validation.constraints.NotBlank;

import java.util.Optional;

public record EpigraphRequest(
        @NotBlank String text,
        @NotBlank String author,
        String source
) {
    public EpigraphComponent toDomain() {
        return new EpigraphComponent(text, author, Optional.ofNullable(source));
    }
}
