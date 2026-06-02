package com.abntbuilder.formatter.document.component.titlepage;

import java.util.Objects;
import java.util.Optional;

public record AcademicPerson(
        String name,
        Optional<String> academicTitle
) {

    public AcademicPerson {
        requireNonBlank(name, "name");
        Objects.requireNonNull(academicTitle, "academicTitle must not be null");
        academicTitle.ifPresent(title -> requireNonBlank(title, "academicTitle"));
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
