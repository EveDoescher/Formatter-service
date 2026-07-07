package com.abntbuilder.formatter.document.component.elementindex;

import com.abntbuilder.formatter.document.component.ComponentType;
import com.abntbuilder.formatter.document.component.DocumentComponent;

public record ElementIndexContent(String componentId) implements DocumentComponent {

    public ElementIndexContent {
        if (componentId == null || componentId.isBlank())
            throw new IllegalArgumentException("componentId must not be blank.");
    }

    @Override
    public ComponentType type() { return ComponentType.ELEMENT_INDEX; }
}
