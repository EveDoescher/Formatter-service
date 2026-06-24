package com.abntbuilder.formatter.document.component.listofabbreviations;

import com.abntbuilder.formatter.document.component.ComponentType;
import com.abntbuilder.formatter.document.component.DocumentComponent;

public record ListOfAbbreviationsComponent() implements DocumentComponent {
    @Override public ComponentType type() { return ComponentType.LIST_OF_ABBREVIATIONS; }
}
