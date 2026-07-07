package com.abntbuilder.formatter.engine.model.content.bodycontent;

import java.util.Objects;
import java.util.Optional;

public record CitationAuthor(
        CitationAuthorType type,
        Optional<String> surname,
        Optional<String> organizationName,
        Optional<String> displayName,
        Optional<String> title
) {

    public CitationAuthor {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(surname, "surname must not be null");
        Objects.requireNonNull(organizationName, "organizationName must not be null");
        Objects.requireNonNull(displayName, "displayName must not be null");
        Objects.requireNonNull(title, "title must not be null");

        surname.ifPresent(value -> requireNonBlank(value, "surname"));
        organizationName.ifPresent(value -> requireNonBlank(value, "organizationName"));
        displayName.ifPresent(value -> requireNonBlank(value, "displayName"));
        title.ifPresent(value -> requireNonBlank(value, "title"));

        switch (type) {
            case PERSON -> {
                requirePresent(surname, "surname");
                requireEmpty(organizationName, "organizationName");
                requireEmpty(displayName, "displayName");
                requireEmpty(title, "title");
            }
            case ORGANIZATION -> {
                requireEmpty(surname, "surname");
                requirePresent(organizationName, "organizationName");
                requireEmpty(title, "title");
            }
            case TITLE -> {
                requireEmpty(surname, "surname");
                requireEmpty(organizationName, "organizationName");
                requireEmpty(displayName, "displayName");
                requirePresent(title, "title");
            }
        }
    }

    public static CitationAuthor person(String surname) {
        return new CitationAuthor(
                CitationAuthorType.PERSON,
                Optional.ofNullable(surname),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
    }

    public String renderedName() {
        return switch (type) {
            case PERSON -> surname.orElseThrow();
            case ORGANIZATION -> displayName.orElseGet(() -> organizationName.orElseThrow());
            case TITLE -> title.orElseThrow();
        };
    }

    private static void requirePresent(Optional<String> value, String fieldName) {
        if (value.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must be provided.");
        }
    }

    private static void requireEmpty(Optional<String> value, String fieldName) {
        if (value.isPresent()) {
            throw new IllegalArgumentException(fieldName + " must not be provided.");
        }
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
