package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.content.references.ReferenceEntry;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record ReferenceEntryRequest(
        @NotBlank String id,
        @NotBlank String type,
        @Valid List<ReferenceAuthorRequest> authors,
        @NotBlank String title,
        String subtitle,
        String edition,
        String city,
        String publisher,
        @NotBlank String year,
        String pages,
        String url,
        String accessDate,
        String volume,
        String issue,
        String doi,
        String degree,
        String institutionName,
        String bookTitle,
        @Valid List<ReferenceAuthorRequest> bookAuthors,
        Map<String, String> customFields
) {
    public ReferenceEntry toDomain() {
        return new ReferenceEntry(
                id,
                type,
                authors == null ? List.of() : authors.stream().map(ReferenceAuthorRequest::toDomain).toList(),
                title,
                Optional.ofNullable(subtitle),
                Optional.ofNullable(edition),
                Optional.ofNullable(city),
                Optional.ofNullable(publisher),
                year,
                Optional.ofNullable(pages),
                Optional.ofNullable(url),
                Optional.ofNullable(accessDate),
                Optional.ofNullable(volume),
                Optional.ofNullable(issue),
                Optional.ofNullable(doi),
                Optional.ofNullable(degree),
                Optional.ofNullable(institutionName),
                Optional.ofNullable(bookTitle),
                Optional.ofNullable(bookAuthors == null ? null :
                        bookAuthors.stream().map(ReferenceAuthorRequest::toDomain).toList()),
                customFields == null ? Map.of() : customFields
        );
    }
}
