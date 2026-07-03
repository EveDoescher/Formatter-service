package com.abntbuilder.formatter.rendering.component;

import com.abntbuilder.formatter.document.component.DocumentComponent;
import com.abntbuilder.formatter.document.component.singlepage.SinglePageContent;
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

            // SinglePageContent renderers share the same Java type but differ by componentId —
            // skip the type-map duplicate check for them.
            if (renderer.componentType() != SinglePageContent.class) {
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

        // SinglePageContent carries its own componentId.
        if (component instanceof SinglePageContent singlePage) {
            if (!renderersByComponentId.containsKey(singlePage.componentId())) {
                throw new MissingComponentRendererException(singlePage.componentId());
            }
            return singlePage.componentId();
        }

        ComponentRenderer<?> renderer = renderersByComponentType.get(component.getClass());

        if (renderer == null) {
            throw new MissingComponentRendererException(component.getClass().getSimpleName());
        }

        return renderer.componentId();
    }
}
