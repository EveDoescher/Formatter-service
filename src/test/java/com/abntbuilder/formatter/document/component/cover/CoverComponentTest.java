package com.abntbuilder.formatter.document.component.cover;

import com.abntbuilder.formatter.document.component.ComponentType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoverComponentTest {

    @Test
    void shouldCreateValidCoverComponent() {
        CoverComponent cover = validCover();

        assertEquals(ComponentType.COVER, cover.type());
        assertEquals(List.of("UNIVERSIDADE PAULISTA"), cover.institutionalLines());
        assertEquals(List.of("NOME DO ALUNO"), cover.authors());
        assertEquals("Titulo do Trabalho", cover.title());
        assertEquals(Optional.of("Subtitulo do trabalho"), cover.subtitle());
        assertEquals("Limeira", cover.city());
        assertEquals("2026", cover.year());
    }

    @Test
    void shouldAllowEmptyOptionalSubtitle() {
        CoverComponent cover = new CoverComponent(
                List.of("UNIVERSIDADE PAULISTA"),
                List.of("NOME DO ALUNO"),
                "Titulo do Trabalho",
                Optional.empty(),
                "Limeira",
                "2026"
        );

        assertTrue(cover.subtitle().isEmpty());
    }

    @Test
    void shouldAllowEmptyInstitutionalLines() {
        CoverComponent cover = new CoverComponent(
                List.of(),
                List.of("NOME DO ALUNO"),
                "Titulo do Trabalho",
                Optional.empty(),
                "Limeira",
                "2026"
        );

        assertTrue(cover.institutionalLines().isEmpty());
    }

    @Test
    void shouldRejectNullInstitutionalLines() {
        assertThrows(NullPointerException.class, () -> new CoverComponent(
                null,
                List.of("NOME DO ALUNO"),
                "Titulo do Trabalho",
                Optional.empty(),
                "Limeira",
                "2026"
        ));
    }

    @Test
    void shouldRejectBlankInstitutionalLineItem() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new CoverComponent(
                List.of(" "),
                List.of("NOME DO ALUNO"),
                "Titulo do Trabalho",
                Optional.empty(),
                "Limeira",
                "2026"
        ));

        assertEquals("institutionalLines item must not be blank.", exception.getMessage());
    }

    @Test
    void shouldRejectBlankAuthorItem() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new CoverComponent(
                List.of("UNIVERSIDADE PAULISTA"),
                List.of(" "),
                "Titulo do Trabalho",
                Optional.empty(),
                "Limeira",
                "2026"
        ));

        assertEquals("authors item must not be blank.", exception.getMessage());
    }

    @Test
    void shouldRejectBlankTitle() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new CoverComponent(
                List.of("UNIVERSIDADE PAULISTA"),
                List.of("NOME DO ALUNO"),
                " ",
                Optional.empty(),
                "Limeira",
                "2026"
        ));

        assertEquals("title must not be blank.", exception.getMessage());
    }

    @Test
    void shouldRejectNullSubtitle() {
        assertThrows(NullPointerException.class, () -> new CoverComponent(
                List.of("UNIVERSIDADE PAULISTA"),
                List.of("NOME DO ALUNO"),
                "Titulo do Trabalho",
                null,
                "Limeira",
                "2026"
        ));
    }

    @Test
    void shouldRejectBlankSubtitleWhenPresent() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new CoverComponent(
                List.of("UNIVERSIDADE PAULISTA"),
                List.of("NOME DO ALUNO"),
                "Titulo do Trabalho",
                Optional.of(" "),
                "Limeira",
                "2026"
        ));

        assertEquals("subtitle must not be blank.", exception.getMessage());
    }

    @Test
    void shouldRejectBlankCity() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new CoverComponent(
                List.of("UNIVERSIDADE PAULISTA"),
                List.of("NOME DO ALUNO"),
                "Titulo do Trabalho",
                Optional.empty(),
                " ",
                "2026"
        ));

        assertEquals("city must not be blank.", exception.getMessage());
    }

    @Test
    void shouldRejectBlankYear() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new CoverComponent(
                List.of("UNIVERSIDADE PAULISTA"),
                List.of("NOME DO ALUNO"),
                "Titulo do Trabalho",
                Optional.empty(),
                "Limeira",
                " "
        ));

        assertEquals("year must not be blank.", exception.getMessage());
    }

    @Test
    void shouldMakeLineListsImmutable() {
        CoverComponent cover = validCover();

        assertThrows(UnsupportedOperationException.class, () -> cover.institutionalLines().add("Outra linha"));
        assertThrows(UnsupportedOperationException.class, () -> cover.authors().add("Outro autor"));
    }

    private static CoverComponent validCover() {
        return new CoverComponent(
                List.of("UNIVERSIDADE PAULISTA"),
                List.of("NOME DO ALUNO"),
                "Titulo do Trabalho",
                Optional.of("Subtitulo do trabalho"),
                "Limeira",
                "2026"
        );
    }
}
