package com.abntbuilder.formatter.document.component.epigraph;

import com.abntbuilder.formatter.document.component.ComponentType;
import com.abntbuilder.formatter.document.component.DocumentComponent;

import java.util.Objects;
import java.util.Optional;

public record EpigraphComponent(
        String text,
        String author,
        Optional<String> source
) implements DocumentComponent {
    public EpigraphComponent {
        requireNonBlank(text, "text");
        requireNonBlank(author, "author");
        Objects.requireNonNull(source, "source must not be null");
    }

    @Override
    public ComponentType type() { return ComponentType.EPIGRAPH; }

    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
