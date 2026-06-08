package com.abntbuilder.formatter.profile.model.component.titlepage;

import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;

public record TitlePageStyleMapping(
        String authorsStyleId,
        String titleStyleId,
        String subtitleStyleId,
        String natureStyleId,
        String advisorStyleId,
        String coadvisorStyleId,
        String cityStyleId,
        String yearStyleId
) {

    public TitlePageStyleMapping {
        requireNonBlank(authorsStyleId, "authorsStyleId");
        requireNonBlank(titleStyleId, "titleStyleId");
        requireNonBlank(subtitleStyleId, "subtitleStyleId");
        requireNonBlank(natureStyleId, "natureStyleId");
        requireNonBlank(advisorStyleId, "advisorStyleId");
        requireNonBlank(coadvisorStyleId, "coadvisorStyleId");
        requireNonBlank(cityStyleId, "cityStyleId");
        requireNonBlank(yearStyleId, "yearStyleId");
    }

    public String styleIdForItem(String itemId) {
        return switch (itemId) {
            case "authors" -> authorsStyleId;
            case "title" -> titleStyleId;
            case "subtitle" -> subtitleStyleId;
            case "nature" -> natureStyleId;
            case "advisor" -> advisorStyleId;
            case "coadvisor" -> coadvisorStyleId;
            case "city" -> cityStyleId;
            case "year" -> yearStyleId;
            default -> throw new InvalidProfileStructureException(
                    "Unknown titlePage style mapping item id: " + itemId
            );
        };
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new InvalidProfileStructureException(fieldName + " must not be blank.");
        }
    }
}
