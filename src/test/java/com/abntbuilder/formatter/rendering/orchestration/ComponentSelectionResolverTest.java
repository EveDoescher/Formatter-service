package com.abntbuilder.formatter.rendering.orchestration;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ComponentSelectionResolverTest {

    private final ComponentSelectionResolver resolver = new ComponentSelectionResolver();

    @Test
    void shouldRenderEveryComponentWhenSelectionIsEmpty() {
        assertTrue(resolver.shouldRender("cover", List.of()));
        assertTrue(resolver.shouldRender("titlePage", List.of()));
    }

    @Test
    void shouldRenderOnlyExplicitlySelectedComponent() {
        assertTrue(resolver.shouldRender("titlePage", List.of("titlePage")));
        assertFalse(resolver.shouldRender("cover", List.of("titlePage")));
    }

    @Test
    void shouldAcceptSupportedSelections() {
        resolver.validateSupportedSelections(
                List.of("cover", "titlePage"),
                Set.of("cover", "titlePage", "paragraphs")
        );
    }

    @Test
    void shouldRejectBlankSelectedComponent() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> resolver.validateSupportedSelections(List.of(" "), Set.of("cover"))
        );

        assertEquals("selectedComponents item must not be blank.", exception.getMessage());
    }

    @Test
    void shouldRejectUnsupportedSelectedComponent() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> resolver.validateSupportedSelections(List.of("abstract"), Set.of("cover"))
        );

        assertEquals("Unsupported selected component: abstract", exception.getMessage());
    }
}
