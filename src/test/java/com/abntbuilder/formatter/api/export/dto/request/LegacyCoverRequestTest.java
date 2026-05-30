package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.cover.CoverComponent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyCoverRequestTest {

    @Test
    void shouldConvertLegacyRootCoverRequestToDomain() {
        LegacyCoverRequest request = new LegacyCoverRequest(
                List.of("Universidade"),
                List.of("Autor"),
                "Titulo",
                null,
                List.of("Limeira", "2026")
        );

        CoverComponent cover = request.toDomain();

        assertEquals(List.of("Universidade"), cover.institutionalLines());
        assertEquals(List.of("Autor"), cover.authors());
        assertEquals("Titulo", cover.title());
        assertEquals("Limeira", cover.city());
        assertEquals("2026", cover.year());
        assertTrue(cover.subtitle().isEmpty());
    }
}
