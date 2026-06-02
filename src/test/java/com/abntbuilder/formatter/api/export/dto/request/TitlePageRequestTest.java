package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.titlepage.TitlePageComponent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TitlePageRequestTest {

    @Test
    void shouldConvertSemanticTitlePageRequestToDomain() {
        TitlePageRequest request = new TitlePageRequest(
                List.of("Autor"),
                "Titulo",
                "Subtitulo",
                new TitlePageNatureRequest(
                        "Trabalho academico",
                        "avaliacao parcial",
                        "Curso",
                        "Universidade"
                ),
                new AcademicPersonRequest("Prof. Dr.", "Jose da Silva"),
                null,
                "Limeira",
                "2026"
        );

        TitlePageComponent titlePage = request.toDomain();

        assertEquals(List.of("Autor"), titlePage.authors());
        assertEquals("Titulo", titlePage.title());
        assertEquals("Subtitulo", titlePage.subtitle().orElseThrow());
        assertEquals("Trabalho academico", titlePage.nature().workType());
        assertEquals("Prof. Dr.", titlePage.advisor().orElseThrow().academicTitle().orElseThrow());
        assertEquals("Jose da Silva", titlePage.advisor().orElseThrow().name());
        assertTrue(titlePage.coadvisor().isEmpty());
        assertEquals("Limeira", titlePage.city());
        assertEquals("2026", titlePage.year());
    }
}
