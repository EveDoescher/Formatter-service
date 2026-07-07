package com.abntbuilder.formatter.engine.model.content.elementindex;

import com.abntbuilder.formatter.engine.model.content.ComponentType;
import com.abntbuilder.formatter.engine.model.content.DocumentComponent;

public record ElementIndexContent(String componentId) implements DocumentComponent {

    public ElementIndexContent {
        if (componentId == null || componentId.isBlank())
            throw new IllegalArgumentException("componentId must not be blank.");
    }

    @Override
    public ComponentType type() { return ComponentType.ELEMENT_INDEX; }
}
