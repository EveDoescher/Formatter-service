package com.abntbuilder.formatter.document.component.listofframes;

import com.abntbuilder.formatter.document.component.ComponentType;
import com.abntbuilder.formatter.document.component.DocumentComponent;

public record ListOfFramesComponent() implements DocumentComponent {
    @Override public ComponentType type() { return ComponentType.LIST_OF_FRAMES; }
}
