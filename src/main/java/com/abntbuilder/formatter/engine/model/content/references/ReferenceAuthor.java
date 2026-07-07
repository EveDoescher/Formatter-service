package com.abntbuilder.formatter.engine.model.content.references;

import java.util.Objects;
import java.util.Optional;

public record ReferenceAuthor(
        String surname,
        Optional<String> givenNames
) {
    public ReferenceAuthor {
        if (surname == null || surname.isBlank()) throw new IllegalArgumentException("surname must not be blank.");
        Objects.requireNonNull(givenNames, "givenNames must not be null");
    }
}
