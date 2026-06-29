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
        Optional<String> accessDate,
        Optional<String> volume,
        Optional<String> issue,
        Optional<String> doi,
        Optional<String> degree,
        Optional<String> institutionName,
        Optional<String> bookTitle,
        Optional<List<ReferenceAuthor>> bookAuthors
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
        Objects.requireNonNull(volume, "volume must not be null");
        Objects.requireNonNull(issue, "issue must not be null");
        Objects.requireNonNull(doi, "doi must not be null");
        Objects.requireNonNull(degree, "degree must not be null");
        Objects.requireNonNull(institutionName, "institutionName must not be null");
        Objects.requireNonNull(bookTitle, "bookTitle must not be null");
        Objects.requireNonNull(bookAuthors, "bookAuthors must not be null");
        authors = List.copyOf(authors);
        bookAuthors = bookAuthors.map(List::copyOf);
    }

    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
