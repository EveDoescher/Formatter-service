package com.abntbuilder.formatter.document.component.bodycontent;

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
                () -> new BodyContentComponent(List.of(
                        BodySection.fromParagraphs("conceitos", 2, Optional.of("Conceitos basicos"), List.of("Texto."))
                ))
        );

        assertEquals("bodyContent section hierarchy cannot start at level 2.", exception.getMessage());
    }

    @Test
    void shouldRejectHierarchyJumpingLevels() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new BodyContentComponent(List.of(
                        BodySection.fromParagraphs("introducao", 1, Optional.of("Introducao"), List.of("Texto.")),
                        BodySection.fromParagraphs("detalhe", 3, Optional.of("Detalhe"), List.of("Texto."))
                ))
        );

        assertEquals("bodyContent section hierarchy cannot jump from level 1 to level 3.", exception.getMessage());
    }

    @Test
    void shouldAllowTitledSectionWithoutBlocksAsGroupingSection() {
        assertDoesNotThrow(() -> new BodyContentComponent(List.of(
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
}
