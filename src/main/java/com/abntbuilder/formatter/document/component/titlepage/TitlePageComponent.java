package com.abntbuilder.formatter.document.component.titlepage;

import com.abntbuilder.formatter.document.component.ComponentType;
import com.abntbuilder.formatter.document.component.DocumentComponent;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record TitlePageComponent(
        List<String> authors,
        String title,
        Optional<String> subtitle,
        TitlePageNature nature,
        Optional<AcademicPerson> advisor,
        Optional<AcademicPerson> coadvisor,
        String city,
        String year
) implements DocumentComponent {

    public TitlePageComponent {
        authors = validateAuthors(authors);
        requireNonBlank(title, "title");
        Objects.requireNonNull(subtitle, "subtitle must not be null");
        subtitle.ifPresent(value -> requireNonBlank(value, "subtitle"));
        Objects.requireNonNull(nature, "nature must not be null");
        Objects.requireNonNull(advisor, "advisor must not be null");
        Objects.requireNonNull(coadvisor, "coadvisor must not be null");
        requireNonBlank(city, "city");
        requireNonBlank(year, "year");
    }

    @Override
    public ComponentType type() {
        return ComponentType.TITLE_PAGE;
    }

    private static List<String> validateAuthors(List<String> authors) {
        Objects.requireNonNull(authors, "authors must not be null");

        if (authors.isEmpty()) {
            throw new IllegalArgumentException("authors must not be empty.");
        }

        for (String author : authors) {
            requireNonBlank(author, "authors item");
        }

        return List.copyOf(authors);
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
