package com.abntbuilder.formatter.document.component.acknowledgments;

import com.abntbuilder.formatter.document.component.ComponentType;
import com.abntbuilder.formatter.document.component.DocumentComponent;

public record AcknowledgmentsComponent(String text) implements DocumentComponent {
    public AcknowledgmentsComponent {
        if (text == null || text.isBlank()) throw new IllegalArgumentException("text must not be blank.");
    }

    @Override
    public ComponentType type() { return ComponentType.ACKNOWLEDGMENTS; }
}
