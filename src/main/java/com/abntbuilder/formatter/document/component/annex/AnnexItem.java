package com.abntbuilder.formatter.document.component.annex;

import com.abntbuilder.formatter.document.component.bodycontent.BodySection;

import java.util.List;
import java.util.Objects;

public record AnnexItem(
        String title,
        List<BodySection> sections
) {
    public AnnexItem {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("title must not be blank.");
        Objects.requireNonNull(sections, "sections must not be null");
        sections = List.copyOf(sections);
    }
}
