package com.abntbuilder.formatter.engine.model.content.bodycontent;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BodyContentComponentTest {

    @Test
    void shouldRejectHierarchyStartingAfterPrimaryLevel() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new BodyContentComponent("bodyContent", List.of(
                        BodySection.fromParagraphs("conceitos", 2, Optional.of("Conceitos basicos"), List.of("Texto."))
                ))
        );

        assertEquals("bodyContent section hierarchy cannot start at level 2.", exception.getMessage());
    }

    @Test
    void shouldRejectHierarchyJumpingLevels() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new BodyContentComponent("bodyContent", List.of(
                        BodySection.fromParagraphs("introducao", 1, Optional.of("Introducao"), List.of("Texto.")),
                        BodySection.fromParagraphs("detalhe", 3, Optional.of("Detalhe"), List.of("Texto."))
                ))
        );

        assertEquals("bodyContent section hierarchy cannot jump from level 1 to level 3.", exception.getMessage());
    }

    @Test
    void shouldAllowTitledSectionWithoutBlocksAsGroupingSection() {
        assertDoesNotThrow(() -> new BodyContentComponent("bodyContent", List.of(
                BodySection.fromParagraphs("desenvolvimento", 1, Optional.of("Desenvolvimento"), List.of()),
                BodySection.fromParagraphs("conceitos", 2, Optional.of("Conceitos basicos"), List.of("Texto.")),
                BodySection.fromParagraphs("aplicacoes", 2, Optional.of("Aplicacoes"), List.of("Texto."))
        )));
    }

    @Test
    void shouldRejectUntitledSectionWithoutBlocks() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> BodySection.fromParagraphs("vazia", 1, Optional.empty(), List.of())
        );

        assertEquals("bodyContent section without title must contain at least one block.", exception.getMessage());
    }

    @Test
    void shouldRejectDuplicatedFigureId() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new BodyContentComponent("bodyContent", List.of(
                        new BodySection(
                                "figuras",
                                1,
                                Optional.of("Figuras"),
                                List.of(figure("figura-teste"), figure("figura-teste"))
                        )
                ))
        );

        assertEquals("bodyContent figure id must be unique: figura-teste", exception.getMessage());
    }

    @Test
    void shouldRejectDuplicatedTableId() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new BodyContentComponent("bodyContent", List.of(
                        new BodySection(
                                "tabelas",
                                1,
                                Optional.of("Tabelas"),
                                List.of(table("tabela-teste"), table("tabela-teste"))
                        )
                ))
        );

        assertEquals("bodyContent table id must be unique: tabela-teste", exception.getMessage());
    }

    private static BodyFigure figure(String id) {
        return new BodyFigure(
                id,
                Optional.of("grupo-figura-teste"),
                "Figura teste",
                Optional.empty(),
                new BodyImageSource(
                        ImageSourceType.DATA_URI,
                        "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/p9sAAAAASUVORK5CYII=",
                        "Imagem teste"
                )
        );
    }

    private static BodyTable table(String id) {
        return new BodyTable(
                id,
                Optional.of("grupo-tabela-teste"),
                "Tabela teste",
                Optional.empty(),
                List.of(new BodyTableColumn("Cenario")),
                List.of(new BodyTableRow(List.of(new BodyTableCell("Teste A"))))
        );
    }
}
