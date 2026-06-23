package com.abntbuilder.formatter.document.component.references;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record ReferenceEntry(
        String id,
        ReferenceType type,
        List<ReferenceAuthor> authors,
        String title,
        Optional<String> subtitle,
        Optional<String> edition,
        Optional<String> city,
        Optional<String> publisher,
        String year,
        Optional<String> pages,
        Optional<String> url,
        Optional<String> accessDate
) {
    public ReferenceEntry {
        requireNonBlank(id, "id");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(authors, "authors must not be null");
        requireNonBlank(title, "title");
        Objects.requireNonNull(subtitle, "subtitle must not be null");
        Objects.requireNonNull(edition, "edition must not be null");
        Objects.requireNonNull(city, "city must not be null");
        Objects.requireNonNull(publisher, "publisher must not be null");
        requireNonBlank(year, "year");
        Objects.requireNonNull(pages, "pages must not be null");
        Objects.requireNonNull(url, "url must not be null");
        Objects.requireNonNull(accessDate, "accessDate must not be null");
        authors = List.copyOf(authors);
    }

    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
