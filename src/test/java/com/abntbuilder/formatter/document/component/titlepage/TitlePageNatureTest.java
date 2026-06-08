package com.abntbuilder.formatter.document.component.titlepage;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TitlePageNatureTest {

    @Test
    void shouldCreateValidNature() {
        TitlePageNature nature = validNature();

        assertEquals("Trabalho de conclusao de curso", nature.workType());
        assertEquals("obtencao do titulo de graduacao", nature.degreeObjective());
    }

    @Test
    void shouldRejectBlankWorkType() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new TitlePageNature(
                        " ",
                        "obtencao do titulo de graduacao",
                        "Analise e Desenvolvimento de Sistemas",
                        "Universidade Paulista"
                )
        );

        assertEquals("workType must not be blank.", exception.getMessage());
    }

    static TitlePageNature validNature() {
        return new TitlePageNature(
                "Trabalho de conclusao de curso",
                "obtencao do titulo de graduacao",
                "Analise e Desenvolvimento de Sistemas",
                "Universidade Paulista"
        );
    }
}
