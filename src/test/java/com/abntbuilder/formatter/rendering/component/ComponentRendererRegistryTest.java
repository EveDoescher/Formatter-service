package com.abntbuilder.formatter.rendering.component;

import com.abntbuilder.formatter.document.component.cover.CoverComponent;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.shared.exception.MissingComponentRendererException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ComponentRendererRegistryTest {

    @Test
    void shouldResolveRegisteredRenderer() {
        ComponentRenderer<CoverComponent> renderer = new FakeCoverRenderer();
        ComponentRendererRegistry registry = new ComponentRendererRegistry(List.of(renderer));

        assertSame(renderer, registry.get("cover"));
    }

    @Test
    void shouldResolveComponentIdForRegisteredComponentType() {
        ComponentRendererRegistry registry = new ComponentRendererRegistry(List.of(new FakeCoverRenderer()));

        assertEquals("cover", registry.componentIdFor(new CoverComponent(
                List.of("Universidade"),
                List.of("Autor"),
                "Titulo",
                Optional.empty(),
                "Limeira",
                "2026"
        )));
    }

    @Test
    void shouldRejectDuplicateRendererComponentType() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ComponentRendererRegistry(List.of(
                        new FakeCoverRenderer(),
                        new AlternativeFakeCoverRenderer()
                ))
        );

        assertEquals("Duplicate component renderer type: CoverComponent", exception.getMessage());
    }

    @Test
    void shouldFailWhenRendererIsMissing() {
        ComponentRendererRegistry registry = new ComponentRendererRegistry(List.of());

        MissingComponentRendererException exception = assertThrows(
                MissingComponentRendererException.class,
                () -> registry.get("titlePage")
        );

        assertEquals("Missing component renderer for id: titlePage", exception.getMessage());
    }

    private static final class FakeCoverRenderer implements ComponentRenderer<CoverComponent> {

        @Override
        public String componentId() {
            return "cover";
        }

        @Override
        public Class<CoverComponent> componentType() {
            return CoverComponent.class;
        }

        @Override
        public List<DocxBlock> render(CoverComponent component, DocumentProfile profile) {
            return List.of();
        }
    }

    private static final class AlternativeFakeCoverRenderer implements ComponentRenderer<CoverComponent> {

        @Override
        public String componentId() {
            return "alternativeCover";
        }

        @Override
        public Class<CoverComponent> componentType() {
            return CoverComponent.class;
        }

        @Override
        public List<DocxBlock> render(CoverComponent component, DocumentProfile profile) {
            return List.of();
        }
    }
}
