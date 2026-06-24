package com.abntbuilder.formatter.document.component.listoffigures;

import com.abntbuilder.formatter.document.component.ComponentType;
import com.abntbuilder.formatter.document.component.DocumentComponent;

public record ListOfFiguresComponent() implements DocumentComponent {
    @Override public ComponentType type() { return ComponentType.LIST_OF_FIGURES; }
}
