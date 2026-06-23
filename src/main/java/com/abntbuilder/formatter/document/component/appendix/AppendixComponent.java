package com.abntbuilder.formatter.document.component.appendix;

import com.abntbuilder.formatter.document.component.ComponentType;
import com.abntbuilder.formatter.document.component.DocumentComponent;

import java.util.List;
import java.util.Objects;

public record AppendixComponent(List<AppendixItem> items) implements DocumentComponent {
    public AppendixComponent {
        Objects.requireNonNull(items, "items must not be null");
        if (items.isEmpty()) throw new IllegalArgumentException("items must not be empty.");
        items = List.copyOf(items);
    }

    @Override
    public ComponentType type() { return ComponentType.APPENDIX; }
}
