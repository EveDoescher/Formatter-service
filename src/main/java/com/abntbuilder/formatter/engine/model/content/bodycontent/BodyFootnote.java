package com.abntbuilder.formatter.engine.model.content.bodycontent;

import java.util.List;
import java.util.Objects;

public record BodyFootnote(
        List<BodyInline> content
) implements BodyInline {
    public BodyFootnote {
        Objects.requireNonNull(content, "content must not be null");
        if (content.isEmpty()) throw new IllegalArgumentException("content must not be empty.");
        content = List.copyOf(content);
    }

    @Override
    public String renderedText() {
        return ""; // rendered as superscript reference, not inline text
    }
}
