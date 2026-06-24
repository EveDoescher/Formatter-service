package com.abntbuilder.formatter.document.component.listoftables;

import com.abntbuilder.formatter.document.component.ComponentType;
import com.abntbuilder.formatter.document.component.DocumentComponent;

public record ListOfTablesComponent() implements DocumentComponent {
    @Override public ComponentType type() { return ComponentType.LIST_OF_TABLES; }
}
