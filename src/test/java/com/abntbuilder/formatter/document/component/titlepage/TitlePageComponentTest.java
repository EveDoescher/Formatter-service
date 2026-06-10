package com.abntbuilder.formatter.document.component.titlepage;

import com.abntbuilder.formatter.document.component.ComponentType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TitlePageComponentTest {

    @Test
    void shouldCreateValidTitlePageComponent() {
        TitlePageComponent titlePage = validTitlePage();

        assertEquals(ComponentType.TITLE_PAGE, titlePage.type());
        assertEquals(List.of("NOME DO ALUNO"), titlePage.authors());
        assertEquals("Titulo do Trabalho", titlePage.title());
        assertEquals(Optional.of("Subtitulo"), titlePage.subtitle());
        assertEquals("Limeira", titlePage.city());
        assertEquals("2026", titlePage.year());
    }

    @Test
    void shouldRejectEmptyAuthors() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new TitlePageComponent(
                        List.of(),
                        "Titulo do Trabalho",
                        Optional.empty(),
                        TitlePageNatureTest.validNature(),
                        Optional.empty(),
                        Optional.empty(),
                        "Limeira",
                        "2026"
                )
        );

        assertEquals("authors must not be empty.", exception.getMessage());
    }

    @Test
    void shouldRejectBlankTitle() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new TitlePageComponent(
                        List.of("NOME DO ALUNO"),
                        " ",
                        Optional.empty(),
                        TitlePageNatureTest.validNature(),
                        Optional.empty(),
                        Optional.empty(),
                        "Limeira",
                        "2026"
                )
        );

        assertEquals("title must not be blank.", exception.getMessage());
    }

    @Test
    void shouldMakeAuthorsImmutable() {
        TitlePageComponent titlePage = validTitlePage();

        assertThrows(UnsupportedOperationException.class, () -> titlePage.authors().add("OUTRO AUTOR"));
    }

    private static TitlePageComponent validTitlePage() {
        return new TitlePageComponent(
                List.of("NOME DO ALUNO"),
                "Titulo do Trabalho",
                Optional.of("Subtitulo"),
                TitlePageNatureTest.validNature(),
                Optional.of(new AcademicPerson("Pessoa Orientadora Teste", Optional.of("Prof. Dr."))),
                Optional.empty(),
                "Limeira",
                "2026"
        );
    }
}
