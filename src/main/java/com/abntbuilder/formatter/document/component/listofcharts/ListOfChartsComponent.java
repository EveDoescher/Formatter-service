package com.abntbuilder.formatter.document.component.listofcharts;

import com.abntbuilder.formatter.document.component.ComponentType;
import com.abntbuilder.formatter.document.component.DocumentComponent;

public record ListOfChartsComponent() implements DocumentComponent {
    @Override public ComponentType type() { return ComponentType.LIST_OF_CHARTS; }
}
