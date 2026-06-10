package com.abntbuilder.formatter.profile.model.component;

import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public record ComponentContentBindings(
        Map<String, String> fieldSources
) {

    private static final Set<String> SUPPORTED_WORK_SOURCES = Set.of(
            "work.institutionalLines",
            "work.authors",
            "work.title",
            "work.subtitle",
            "work.nature",
            "work.advisor",
            "work.coadvisor",
            "work.city",
            "work.year"
    );

    public ComponentContentBindings {
        Objects.requireNonNull(fieldSources, "fieldSources must not be null");
        fieldSources = Map.copyOf(fieldSources);

        for (Map.Entry<String, String> entry : fieldSources.entrySet()) {
            requireNonBlank(entry.getKey(), "contentBindings field");
            requireNonBlank(entry.getValue(), "contentBindings source");
            requireSupportedSource(entry.getValue());
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

    private static void requireSupportedSource(String source) {
        if (!SUPPORTED_WORK_SOURCES.contains(source)) {
            throw new InvalidProfileStructureException("Unsupported content binding source: " + source);
        }
    }
}
