package com.abntbuilder.formatter.profile.model.component;

import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record ComponentContentBindings(
        Map<String, String> fieldSources
) {

    public ComponentContentBindings {
        Objects.requireNonNull(fieldSources, "fieldSources must not be null");
        fieldSources = Map.copyOf(fieldSources);

        for (Map.Entry<String, String> entry : fieldSources.entrySet()) {
            requireNonBlank(entry.getKey(), "contentBindings field");
            requireNonBlank(entry.getValue(), "contentBindings source");
        }
    }

    public Optional<String> sourceFor(String fieldName) {
        return Optional.ofNullable(fieldSources.get(fieldName));
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new InvalidProfileStructureException(fieldName + " must not be blank.");
        }
    }
}
