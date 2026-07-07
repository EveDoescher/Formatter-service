package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.content.references.ReferenceAuthor;
import jakarta.validation.constraints.NotBlank;

import java.util.Optional;

public record ReferenceAuthorRequest(
        @NotBlank String surname,
        String givenNames
) {
    public ReferenceAuthor toDomain() {
        return new ReferenceAuthor(surname, Optional.ofNullable(givenNames));
    }
}
