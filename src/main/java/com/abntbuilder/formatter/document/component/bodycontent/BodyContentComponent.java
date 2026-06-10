package com.abntbuilder.formatter.document.component.bodycontent;

import com.abntbuilder.formatter.document.component.ComponentType;
import com.abntbuilder.formatter.document.component.DocumentComponent;

import java.util.List;
import java.util.Objects;

public record BodyContentComponent(
        List<BodySection> sections
) implements DocumentComponent {

    public BodyContentComponent {
        Objects.requireNonNull(sections, "sections must not be null");

        if (sections.isEmpty()) {
            throw new IllegalArgumentException("sections must not be empty.");
        }

        sections = List.copyOf(sections);

        for (BodySection section : sections) {
            Objects.requireNonNull(section, "sections must not contain null values.");
        }
    }

    @Override
    public ComponentType type() {
        return ComponentType.BODY_CONTENT;
    }
}
