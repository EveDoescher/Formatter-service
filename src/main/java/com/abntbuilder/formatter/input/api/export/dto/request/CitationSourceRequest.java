package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.content.bodycontent.CitationSource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Optional;

public record CitationSourceRequest(
        @Valid List<CitationAuthorRequest> authors,
        @NotBlank String year,
        String page
) {

    CitationSource toDomain() {
        return new CitationSource(
                authors == null
                        ? List.of()
                        : authors.stream().map(CitationAuthorRequest::toDomain).toList(),
                year,
                page == null ? Optional.empty() : Optional.of(page)
        );
    }
}
