package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.cover.CoverComponent;

import java.util.List;
import java.util.Optional;

@Deprecated(since = "cover-semantic-request")
public record LegacyCoverRequest(
        List<String> topLines,
        List<String> authorLines,
        String title,
        String subtitle,
        List<String> bottomLines
) {

    public CoverComponent toDomain() {
        List<String> validatedBottomLines = validateBottomLines();

        return new CoverComponent(
                validateLines(topLines == null ? List.of() : topLines, "topLines"),
                validateLines(authorLines == null ? List.of() : authorLines, "authorLines"),
                title,
                subtitle == null ? Optional.empty() : Optional.of(subtitle),
                validatedBottomLines.get(0),
                validatedBottomLines.get(1)
        );
    }

    private List<String> validateBottomLines() {
        List<String> validatedBottomLines = validateLines(
                bottomLines == null ? List.of() : bottomLines,
                "bottomLines"
        );

        if (validatedBottomLines.size() != 2) {
            throw new IllegalArgumentException("bottomLines must contain exactly city and year.");
        }

        return validatedBottomLines;
    }

    private static List<String> validateLines(List<String> lines, String fieldName) {
        for (String line : lines) {
            if (line == null || line.isBlank()) {
                throw new IllegalArgumentException(fieldName + " item must not be blank.");
            }
        }

        return List.copyOf(lines);
    }
}
