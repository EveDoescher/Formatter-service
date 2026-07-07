package com.abntbuilder.formatter.engine.model.content.sectioned;

import com.abntbuilder.formatter.engine.model.content.bodycontent.BodySection;

import java.util.List;
import java.util.Objects;

public record SectionedItem(
        String title,
        List<BodySection> sections
) {
    public SectionedItem {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("title must not be blank.");
        Objects.requireNonNull(sections, "sections must not be null");
        sections = List.copyOf(sections);
    }
}
