package com.abntbuilder.formatter.document.component.sectionindex;

import com.abntbuilder.formatter.document.component.ComponentType;
import com.abntbuilder.formatter.document.component.DocumentComponent;

public record SectionIndexContent(String componentId) implements DocumentComponent {

    public SectionIndexContent {
        if (componentId == null || componentId.isBlank())
            throw new IllegalArgumentException("componentId must not be blank.");
    }

    @Override
    public ComponentType type() { return ComponentType.SECTION_INDEX; }
}
