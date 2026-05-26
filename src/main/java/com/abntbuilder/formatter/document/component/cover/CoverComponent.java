package com.abntbuilder.formatter.document.component.cover;

import com.abntbuilder.formatter.document.component.ComponentType;
import com.abntbuilder.formatter.document.component.DocumentComponent;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record CoverComponent(
        List<String> topLines,
        List<String> authorLines,
        String title,
        Optional<String> subtitle,
        List<String> bottomLines
) implements DocumentComponent {

    public CoverComponent {
        topLines = validateLines(topLines, "topLines");
        authorLines = validateLines(authorLines, "authorLines");
        requireNonBlank(title, "title");
        Objects.requireNonNull(subtitle, "subtitle must not be null");
        validateOptionalText(subtitle, "subtitle");
        bottomLines = validateLines(bottomLines, "bottomLines");
    }

    @Override
    public ComponentType type() {
        return ComponentType.COVER;
    }

    private static List<String> validateLines(List<String> lines, String fieldName) {
        Objects.requireNonNull(lines, fieldName + " must not be null");

        for (String line : lines) {
            requireNonBlank(line, fieldName + " item");
        }

        return List.copyOf(lines);
    }

    private static void validateOptionalText(Optional<String> value, String fieldName) {
        value.ifPresent(text -> requireNonBlank(text, fieldName));
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}