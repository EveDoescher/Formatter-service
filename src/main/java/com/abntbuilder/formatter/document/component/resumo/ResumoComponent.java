package com.abntbuilder.formatter.document.component.resumo;

import com.abntbuilder.formatter.document.component.ComponentType;
import com.abntbuilder.formatter.document.component.DocumentComponent;

import java.util.List;
import java.util.Objects;

public record ResumoComponent(
        String text,
        List<String> keywords
) implements DocumentComponent {
    public ResumoComponent {
        if (text == null || text.isBlank()) throw new IllegalArgumentException("text must not be blank.");
        Objects.requireNonNull(keywords, "keywords must not be null");
        if (keywords.isEmpty()) throw new IllegalArgumentException("keywords must not be empty.");
        keywords = List.copyOf(keywords);
    }

    @Override
    public ComponentType type() { return ComponentType.RESUMO; }
}
