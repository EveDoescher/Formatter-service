package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.cover.CoverComponent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoverRequestTest {

    @Test
    void shouldConvertSemanticCoverRequestToDomain() {
        CoverRequest request = new CoverRequest(
                List.of("Universidade"),
                List.of("Autor"),
                "Titulo",
                "Subtitulo",
                "Limeira",
                "2026",
                null,
                null,
                null
        );

        CoverComponent cover = request.toDomain();

        assertEquals(List.of("Universidade"), cover.institutionalLines());
        assertEquals(List.of("Autor"), cover.authors());
        assertEquals("Titulo", cover.title());
        assertEquals("Subtitulo", cover.subtitle().orElseThrow());
        assertEquals("Limeira", cover.city());
        assertEquals("2026", cover.year());
    }

    @Test
    void shouldKeepLegacyCoverRequestAsCompatibilityPathOnly() {
        CoverRequest request = new CoverRequest(
                null,
                null,
                "Titulo",
                null,
                null,
                null,
                List.of("Universidade"),
                List.of("Autor"),
                List.of("Limeira", "2026")
        );

        CoverComponent cover = request.toDomain();

        assertEquals(List.of("Universidade"), cover.institutionalLines());
        assertEquals(List.of("Autor"), cover.authors());
        assertEquals("Limeira", cover.city());
        assertEquals("2026", cover.year());
        assertTrue(cover.subtitle().isEmpty());
    }
}
