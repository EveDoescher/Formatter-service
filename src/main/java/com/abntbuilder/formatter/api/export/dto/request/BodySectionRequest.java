package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.bodycontent.BodySection;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Optional;

public record BodySectionRequest(
        @NotBlank String id,
        @NotNull @Min(1) @Max(6) Integer level,
        String title,
        @NotEmpty List<@NotBlank String> paragraphs
) {

    public BodySection toDomain() {
        return new BodySection(
                id,
                level,
                title == null ? Optional.empty() : Optional.of(title),
                paragraphs == null ? List.of() : paragraphs
        );
    }
}
