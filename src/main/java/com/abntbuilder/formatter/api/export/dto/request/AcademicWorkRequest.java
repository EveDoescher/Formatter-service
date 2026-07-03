package com.abntbuilder.formatter.api.export.dto.request;

import java.util.List;
import java.util.Map;

// Kept for JSON backwards compatibility — fields are accepted but ignored by the rendering engine.
// Content for single-page components is now provided directly via their slot maps.
public record AcademicWorkRequest(
        List<String> institutionalLines,
        List<String> authors,
        String title,
        String subtitle,
        Map<String, String> nature,
        Map<String, String> advisor,
        Map<String, String> coadvisor,
        String city,
        String year
) {}
