package com.abntbuilder.formatter.engine.model.content.sectionindex;

import com.abntbuilder.formatter.engine.model.content.ComponentType;
import com.abntbuilder.formatter.engine.model.content.DocumentComponent;

public record SectionIndexContent(String componentId) implements DocumentComponent {

    public SectionIndexContent {
        if (componentId == null || componentId.isBlank())
            throw new IllegalArgumentException("componentId must not be blank.");
    }

    @Override
    public ComponentType type() { return ComponentType.SECTION_INDEX; }
}
