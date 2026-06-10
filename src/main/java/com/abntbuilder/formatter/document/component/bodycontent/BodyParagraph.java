package com.abntbuilder.formatter.document.component.bodycontent;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public record BodyParagraph(
        List<BodyInline> content
) implements BodyBlock {

    public BodyParagraph(String text) {
        this(List.of(new BodyText(text)));
    }

    public BodyParagraph {
        Objects.requireNonNull(content, "content must not be null");

        if (content.isEmpty()) {
            throw new IllegalArgumentException("content must not be empty.");
        }

        content = List.copyOf(content);

        for (BodyInline inline : content) {
            Objects.requireNonNull(inline, "content must not contain null values.");
        }
    }

    public String text() {
        return content.stream()
                .map(BodyInline::renderedText)
                .collect(Collectors.joining());
    }
}
