package com.abntbuilder.formatter.api.export.dto.request;

import jakarta.validation.Valid;

import java.util.List;

public record AcademicWorkRequest(
        List<String> institutionalLines,
        List<String> authors,
        String title,
        String subtitle,
        @Valid AcademicWorkNatureRequest nature,
        @Valid AcademicPersonRequest advisor,
        @Valid AcademicPersonRequest coadvisor,
        String city,
        String year
) {

    Object valueFor(String source) {
        return switch (source) {
            case "work.institutionalLines" -> institutionalLines;
            case "work.authors" -> authors;
            case "work.title" -> title;
            case "work.subtitle" -> subtitle;
            case "work.nature" -> nature;
            case "work.advisor" -> advisor;
            case "work.coadvisor" -> coadvisor;
            case "work.city" -> city;
            case "work.year" -> year;
            default -> throw new IllegalArgumentException("Unsupported work binding source: " + source);
        };
    }
}
