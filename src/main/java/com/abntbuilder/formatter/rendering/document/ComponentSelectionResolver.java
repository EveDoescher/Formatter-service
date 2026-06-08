package com.abntbuilder.formatter.rendering.document;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class ComponentSelectionResolver {

    public boolean shouldRender(String componentId, List<String> selectedComponents) {
        if (componentId == null || componentId.isBlank()) {
            throw new IllegalArgumentException("componentId must not be blank.");
        }

        Objects.requireNonNull(selectedComponents, "selectedComponents must not be null");

        return selectedComponents.isEmpty() || selectedComponents.contains(componentId);
    }

    public void validateSupportedSelections(
            List<String> selectedComponents,
            Set<String> supportedComponentIds
    ) {
        Objects.requireNonNull(selectedComponents, "selectedComponents must not be null");
        Objects.requireNonNull(supportedComponentIds, "supportedComponentIds must not be null");

        for (String componentId : selectedComponents) {
            if (componentId == null || componentId.isBlank()) {
                throw new IllegalArgumentException("selectedComponents item must not be blank.");
            }

            if (!supportedComponentIds.contains(componentId)) {
                throw new IllegalArgumentException("Unsupported selected component: " + componentId);
            }
        }
    }
}
