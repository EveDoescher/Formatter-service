package com.abntbuilder.formatter.document.component.cover;

import com.abntbuilder.formatter.document.component.ComponentType;
import com.abntbuilder.formatter.document.component.DocumentComponent;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record CoverComponent(
        List<String> institutionalLines,
        List<String> authors,
        String title,
        Optional<String> subtitle,
        String city,
        String year
) implements DocumentComponent {

    public CoverComponent {
        institutionalLines = validateLines(institutionalLines, "institutionalLines");
        authors = validateLines(authors, "authors");
        requireNonBlank(title, "title");
        Objects.requireNonNull(subtitle, "subtitle must not be null");
        validateOptionalText(subtitle, "subtitle");
        requireNonBlank(city, "city");
        requireNonBlank(year, "year");
    }

    public CoverComponent(
            List<String> topLines,
            List<String> authorLines,
            String title,
            Optional<String> subtitle,
            List<String> bottomLines
    ) {
        this(
                validateLines(topLines, "topLines"),
                validateLines(authorLines, "authorLines"),
                title,
                subtitle,
                cityFromBottomLines(bottomLines),
                yearFromBottomLines(bottomLines)
        );
    }

    @Override
    public ComponentType type() {
        return ComponentType.COVER;
    }

    public List<String> topLines() {
        return institutionalLines;
    }

    public List<String> authorLines() {
        return authors;
    }

    public List<String> bottomLines() {
        return List.of(city, year);
    }

    private static String cityFromBottomLines(List<String> bottomLines) {
        validateLegacyBottomLines(bottomLines);

        return bottomLines.get(0);
    }

    private static String yearFromBottomLines(List<String> bottomLines) {
        validateLegacyBottomLines(bottomLines);

        return bottomLines.get(1);
    }

    private static void validateLegacyBottomLines(List<String> bottomLines) {
        List<String> validatedBottomLines = validateLines(bottomLines, "bottomLines");

        if (validatedBottomLines.size() != 2) {
            throw new IllegalArgumentException("bottomLines must contain exactly city and year.");
        }
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
