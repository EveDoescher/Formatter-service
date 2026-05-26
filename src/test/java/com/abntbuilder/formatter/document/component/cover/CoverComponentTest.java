package com.abntbuilder.formatter.document.component.cover;

import com.abntbuilder.formatter.document.component.ComponentType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CoverComponentTest {

    @Test
    void shouldCreateValidCoverComponent() {
        CoverComponent cover = new CoverComponent(
                List.of("UNIVERSIDADE PAULISTA"),
                List.of("NOME DO ALUNO"),
                "Título do Trabalho",
                Optional.of("Subtítulo do trabalho"),
                List.of("Limeira", "2026")
        );

        assertEquals(ComponentType.COVER, cover.type());
        assertEquals(List.of("UNIVERSIDADE PAULISTA"), cover.topLines());
        assertEquals(List.of("NOME DO ALUNO"), cover.authorLines());
        assertEquals("Título do Trabalho", cover.title());
        assertEquals(Optional.of("Subtítulo do trabalho"), cover.subtitle());
        assertEquals(List.of("Limeira", "2026"), cover.bottomLines());
    }

    @Test
    void shouldAllowEmptyOptionalSubtitle() {
        CoverComponent cover = new CoverComponent(
                List.of("UNIVERSIDADE PAULISTA"),
                List.of("NOME DO ALUNO"),
                "Título do Trabalho",
                Optional.empty(),
                List.of("Limeira", "2026")
        );

        assertTrue(cover.subtitle().isEmpty());
    }

    @Test
    void shouldAllowEmptyTopLines() {
        CoverComponent cover = new CoverComponent(
                List.of(),
                List.of("NOME DO ALUNO"),
                "Título do Trabalho",
                Optional.empty(),
                List.of("Limeira", "2026")
        );

        assertTrue(cover.topLines().isEmpty());
    }

    @Test
    void shouldRejectNullTopLines() {
        assertThrows(NullPointerException.class, () -> new CoverComponent(
                null,
                List.of("NOME DO ALUNO"),
                "Título do Trabalho",
                Optional.empty(),
                List.of("Limeira", "2026")
        ));
    }

    @Test
    void shouldRejectBlankTopLineItem() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new CoverComponent(
                List.of(" "),
                List.of("NOME DO ALUNO"),
                "Título do Trabalho",
                Optional.empty(),
                List.of("Limeira", "2026")
        ));

        assertEquals("topLines item must not be blank.", exception.getMessage());
    }

    @Test
    void shouldRejectBlankAuthorLineItem() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new CoverComponent(
                List.of("UNIVERSIDADE PAULISTA"),
                List.of(" "),
                "Título do Trabalho",
                Optional.empty(),
                List.of("Limeira", "2026")
        ));

        assertEquals("authorLines item must not be blank.", exception.getMessage());
    }

    @Test
    void shouldRejectBlankTitle() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new CoverComponent(
                List.of("UNIVERSIDADE PAULISTA"),
                List.of("NOME DO ALUNO"),
                " ",
                Optional.empty(),
                List.of("Limeira", "2026")
        ));

        assertEquals("title must not be blank.", exception.getMessage());
    }

    @Test
    void shouldRejectNullSubtitle() {
        assertThrows(NullPointerException.class, () -> new CoverComponent(
                List.of("UNIVERSIDADE PAULISTA"),
                List.of("NOME DO ALUNO"),
                "Título do Trabalho",
                null,
                List.of("Limeira", "2026")
        ));
    }

    @Test
    void shouldRejectBlankSubtitleWhenPresent() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new CoverComponent(
                List.of("UNIVERSIDADE PAULISTA"),
                List.of("NOME DO ALUNO"),
                "Título do Trabalho",
                Optional.of(" "),
                List.of("Limeira", "2026")
        ));

        assertEquals("subtitle must not be blank.", exception.getMessage());
    }

    @Test
    void shouldRejectBlankBottomLineItem() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new CoverComponent(
                List.of("UNIVERSIDADE PAULISTA"),
                List.of("NOME DO ALUNO"),
                "Título do Trabalho",
                Optional.empty(),
                List.of("Limeira", " ")
        ));

        assertEquals("bottomLines item must not be blank.", exception.getMessage());
    }

    @Test
    void shouldMakeLineListsImmutable() {
        CoverComponent cover = new CoverComponent(
                List.of("UNIVERSIDADE PAULISTA"),
                List.of("NOME DO ALUNO"),
                "Título do Trabalho",
                Optional.empty(),
                List.of("Limeira", "2026")
        );

        assertThrows(UnsupportedOperationException.class, () -> cover.topLines().add("Outra linha"));
        assertThrows(UnsupportedOperationException.class, () -> cover.authorLines().add("Outro autor"));
        assertThrows(UnsupportedOperationException.class, () -> cover.bottomLines().add("Outra linha"));
    }
}