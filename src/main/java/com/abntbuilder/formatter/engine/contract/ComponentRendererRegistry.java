package com.abntbuilder.formatter.engine.contract;

import com.abntbuilder.formatter.engine.model.content.DocumentComponent;
import com.abntbuilder.formatter.engine.model.content.elementindex.ElementIndexContent;
import com.abntbuilder.formatter.engine.model.content.flowtextual.FlowTextualContent;
import com.abntbuilder.formatter.engine.model.content.sectionindex.SectionIndexContent;
import com.abntbuilder.formatter.engine.model.content.sectioned.SectionedContent;
import com.abntbuilder.formatter.engine.model.content.singlepage.SinglePageContent;
import com.abntbuilder.formatter.shared.exception.MissingComponentRendererException;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ComponentRendererRegistry {

    private final Map<String, ComponentRenderer<?>> renderersByComponentId;
    private final Map<Class<? extends DocumentComponent>, ComponentRenderer<?>> renderersByComponentType;

    public ComponentRendererRegistry(List<ComponentRenderer<?>> renderers) {
        Objects.requireNonNull(renderers, "renderers must not be null");

        Map<String, ComponentRenderer<?>> resolvedRenderers = new LinkedHashMap<>();
        Map<Class<? extends DocumentComponent>, ComponentRenderer<?>> resolvedRenderersByType = new HashMap<>();

        for (ComponentRenderer<?> renderer : renderers) {
            Objects.requireNonNull(renderer, "renderers must not contain null values.");

            if (resolvedRenderers.put(renderer.componentId(), renderer) != null) {
                throw new IllegalArgumentException("Duplicate component renderer id: " + renderer.componentId());
            }

            // Components that carry their own componentId share the same Java type but differ by
            // componentId — skip the type-map duplicate check for them.
            if (renderer.componentType() != SinglePageContent.class
                    && renderer.componentType() != FlowTextualContent.class
                    && renderer.componentType() != SectionedContent.class
                    && renderer.componentType() != SectionIndexContent.class
                    && renderer.componentType() != ElementIndexContent.class) {
                if (resolvedRenderersByType.put(renderer.componentType(), renderer) != null) {
                    throw new IllegalArgumentException(
                            "Duplicate component renderer type: " + renderer.componentType().getSimpleName()
                    );
                }
            }
        }

        renderersByComponentId = Map.copyOf(resolvedRenderers);
        renderersByComponentType = Map.copyOf(resolvedRenderersByType);
    }

    public ComponentRenderer<?> get(String componentId) {
        if (componentId == null || componentId.isBlank()) {
            throw new IllegalArgumentException("componentId must not be blank.");
        }

        ComponentRenderer<?> renderer = renderersByComponentId.get(componentId);

        if (renderer == null) {
            throw new MissingComponentRendererException(componentId);
        }

        return renderer;
    }

    public String componentIdFor(DocumentComponent component) {
        Objects.requireNonNull(component, "component must not be null");

        // Components that carry their own componentId: resolve by id rather than by class.
        String selfId = selfComponentId(component);
        if (selfId != null) {
            if (!renderersByComponentId.containsKey(selfId)) {
                throw new MissingComponentRendererException(selfId);
            }
            return selfId;
        }

        ComponentRenderer<?> renderer = renderersByComponentType.get(component.getClass());

        if (renderer == null) {
            throw new MissingComponentRendererException(component.getClass().getSimpleName());
        }

        return renderer.componentId();
    }

    private static String selfComponentId(DocumentComponent component) {
        if (component instanceof SinglePageContent c) return c.componentId();
        if (component instanceof FlowTextualContent c) return c.componentId();
        if (component instanceof SectionedContent c) return c.componentId();
        if (component instanceof SectionIndexContent c) return c.componentId();
        if (component instanceof ElementIndexContent c) return c.componentId();
        return null;
    }
}
