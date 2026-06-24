package com.abntbuilder.formatter.document.component.listofcodelistings;

import com.abntbuilder.formatter.document.component.ComponentType;
import com.abntbuilder.formatter.document.component.DocumentComponent;

public record ListOfCodeListingsComponent() implements DocumentComponent {
    @Override public ComponentType type() { return ComponentType.LIST_OF_CODE_LISTINGS; }
}
