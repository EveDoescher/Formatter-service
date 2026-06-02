package com.abntbuilder.formatter.rendering.component;

import com.abntbuilder.formatter.shared.exception.MissingComponentRendererException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ComponentRendererRegistry {

    private final Map<String, ComponentRenderer<?>> renderersByComponentId;

    public ComponentRendererRegistry(List<ComponentRenderer<?>> renderers) {
        Objects.requireNonNull(renderers, "renderers must not be null");

        Map<String, ComponentRenderer<?>> resolvedRenderers = new LinkedHashMap<>();

        for (ComponentRenderer<?> renderer : renderers) {
            Objects.requireNonNull(renderer, "renderers must not contain null values.");

            if (resolvedRenderers.put(renderer.componentId(), renderer) != null) {
                throw new IllegalArgumentException("Duplicate component renderer id: " + renderer.componentId());
            }
        }

        renderersByComponentId = Map.copyOf(resolvedRenderers);
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
}
