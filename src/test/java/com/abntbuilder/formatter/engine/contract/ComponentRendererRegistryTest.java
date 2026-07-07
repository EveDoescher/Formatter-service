package com.abntbuilder.formatter.engine.contract;

import com.abntbuilder.formatter.engine.model.content.singlepage.SinglePageContent;
import com.abntbuilder.formatter.engine.model.output.DocxBlock;
import com.abntbuilder.formatter.engine.model.profile.DocumentProfile;
import com.abntbuilder.formatter.shared.exception.MissingComponentRendererException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ComponentRendererRegistryTest {

    @Test
    void shouldResolveRegisteredRenderer() {
        ComponentRenderer<SinglePageContent> renderer = new FakeCoverRenderer();
        ComponentRendererRegistry registry = new ComponentRendererRegistry(List.of(renderer));

        assertSame(renderer, registry.get("cover"));
    }

    @Test
    void shouldResolveComponentIdForRegisteredSinglePageRenderer() {
        ComponentRendererRegistry registry = new ComponentRendererRegistry(List.of(new FakeCoverRenderer()));

        assertEquals("cover", registry.componentIdFor(new SinglePageContent("cover", Map.of())));
    }

    @Test
    void shouldAllowMultipleSinglePageRenderersWithDifferentComponentIds() {
        // SinglePageContent renderers share a Java type but differ by componentId — all valid
        assertDoesNotThrow(() -> new ComponentRendererRegistry(List.of(
                new FakeCoverRenderer(),
                new AlternativeFakeCoverRenderer()
        )));
    }

    @Test
    void shouldResolveComponentIdDirectlyFromSinglePageContentComponentId() {
        ComponentRendererRegistry registry = new ComponentRendererRegistry(List.of(new FakeCoverRenderer()));

        assertEquals("cover", registry.componentIdFor(new SinglePageContent("cover", java.util.Map.of())));
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

    private static final class FakeCoverRenderer implements ComponentRenderer<SinglePageContent> {

        @Override
        public String componentId() {
            return "cover";
        }

        @Override
        public Class<SinglePageContent> componentType() {
            return SinglePageContent.class;
        }

        @Override
        public List<DocxBlock> render(SinglePageContent component, DocumentProfile profile) {
            return List.of();
        }
    }

    private static final class AlternativeFakeCoverRenderer implements ComponentRenderer<SinglePageContent> {

        @Override
        public String componentId() {
            return "alternativeCover";
        }

        @Override
        public Class<SinglePageContent> componentType() {
            return SinglePageContent.class;
        }

        @Override
        public List<DocxBlock> render(SinglePageContent component, DocumentProfile profile) {
            return List.of();
        }
    }
}
