package com.abntbuilder.formatter.profile.model.component.cover;

import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;

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

    public String styleIdForItem(String itemId) {
        return switch (itemId) {
            case "institutionalLines" -> institutionalLinesStyleId;
            case "authors" -> authorsStyleId;
            case "title" -> titleStyleId;
            case "subtitle" -> subtitleStyleId;
            case "city" -> cityStyleId;
            case "year" -> yearStyleId;
            default -> throw new InvalidProfileStructureException("Unknown cover style mapping item id: " + itemId);
        };
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new InvalidProfileStructureException(fieldName + " must not be blank.");
        }
    }
}
