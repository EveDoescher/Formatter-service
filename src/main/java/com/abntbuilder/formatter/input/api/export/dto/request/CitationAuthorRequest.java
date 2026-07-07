package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.content.bodycontent.CitationAuthor;
import com.abntbuilder.formatter.engine.model.content.bodycontent.CitationAuthorType;
import jakarta.validation.constraints.NotNull;

import java.util.Optional;

public record CitationAuthorRequest(
        @NotNull CitationAuthorType type,
        String surname,
        String organizationName,
        String displayName,
        String title
) {

    CitationAuthor toDomain() {
        return new CitationAuthor(
                type,
                surname == null ? Optional.empty() : Optional.of(surname),
                organizationName == null ? Optional.empty() : Optional.of(organizationName),
                displayName == null ? Optional.empty() : Optional.of(displayName),
                title == null ? Optional.empty() : Optional.of(title)
        );
    }
}
