package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.references.ReferenceAuthor;
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
