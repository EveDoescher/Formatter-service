package com.abntbuilder.formatter.profile.model.component.cover;

public record CoverStyleMapping(
        String institutionalLinesStyleId,
        String authorsStyleId,
        String titleStyleId,
        String subtitleStyleId,
        String cityStyleId,
        String yearStyleId
) {

    public CoverStyleMapping {
        requireNonBlank(institutionalLinesStyleId, "institutionalLinesStyleId");
        requireNonBlank(authorsStyleId, "authorsStyleId");
        requireNonBlank(titleStyleId, "titleStyleId");
        requireNonBlank(subtitleStyleId, "subtitleStyleId");
        requireNonBlank(cityStyleId, "cityStyleId");
        requireNonBlank(yearStyleId, "yearStyleId");
    }

    public CoverStyleMapping(
            String topLinesStyleId,
            String authorLinesStyleId,
            String titleStyleId,
            String subtitleStyleId,
            String bottomLinesStyleId
    ) {
        this(
                requireLegacyNonBlank(topLinesStyleId, "topLinesStyleId"),
                requireLegacyNonBlank(authorLinesStyleId, "authorLinesStyleId"),
                requireLegacyNonBlank(titleStyleId, "titleStyleId"),
                requireLegacyNonBlank(subtitleStyleId, "subtitleStyleId"),
                requireLegacyNonBlank(bottomLinesStyleId, "bottomLinesStyleId"),
                requireLegacyNonBlank(bottomLinesStyleId, "bottomLinesStyleId")
        );
    }

    public String topLinesStyleId() {
        return institutionalLinesStyleId;
    }

    public String authorLinesStyleId() {
        return authorsStyleId;
    }

    public String bottomLinesStyleId() {
        return cityStyleId;
    }

    public String styleIdForItem(String itemId) {
        return switch (itemId) {
            case "institutionalLines" -> institutionalLinesStyleId;
            case "authors" -> authorsStyleId;
            case "title" -> titleStyleId;
            case "subtitle" -> subtitleStyleId;
            case "city" -> cityStyleId;
            case "year" -> yearStyleId;
            default -> throw new IllegalArgumentException("Unknown cover style mapping item id: " + itemId);
        };
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }

    private static String requireLegacyNonBlank(String value, String fieldName) {
        requireNonBlank(value, fieldName);
        return value;
    }
}
