package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.titlepage.TitlePageComponent;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Optional;

public record TitlePageRequest(
        @NotEmpty List<@NotBlank String> authors,
        @NotBlank String title,
        String subtitle,
        @Valid @NotNull TitlePageNatureRequest nature,
        @Valid AcademicPersonRequest advisor,
        @Valid AcademicPersonRequest coadvisor,
        @NotBlank String city,
        @NotBlank String year
) {

    public TitlePageComponent toDomain() {
        return new TitlePageComponent(
                authors == null ? List.of() : authors,
                title,
                subtitle == null ? Optional.empty() : Optional.of(subtitle),
                nature.toDomain(),
                advisor == null ? Optional.empty() : Optional.of(advisor.toDomain()),
                coadvisor == null ? Optional.empty() : Optional.of(coadvisor.toDomain()),
                city,
                year
        );
    }
}
