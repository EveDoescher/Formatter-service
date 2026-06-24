package com.abntbuilder.formatter.document.component.summary;

import com.abntbuilder.formatter.document.component.ComponentType;
import com.abntbuilder.formatter.document.component.DocumentComponent;

public record SummaryComponent() implements DocumentComponent {
    @Override public ComponentType type() { return ComponentType.SUMMARY; }
}
