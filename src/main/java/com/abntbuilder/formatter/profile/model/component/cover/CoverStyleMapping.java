package com.abntbuilder.formatter.profile.model.component.cover;

public record CoverStyleMapping(
        String topLinesStyleId,
        String authorLinesStyleId,
        String titleStyleId,
        String subtitleStyleId,
        String bottomLinesStyleId
) {
    public CoverStyleMapping {
        requireNonBlank(topLinesStyleId, "topLinesStyleId");
        requireNonBlank(authorLinesStyleId, "authorLinesStyleId");
        requireNonBlank(titleStyleId, "titleStyleId");
        requireNonBlank(subtitleStyleId, "subtitleStyleId");
        requireNonBlank(bottomLinesStyleId, "bottomLinesStyleId");
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}