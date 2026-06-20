package com.abntbuilder.formatter.document.component.bodycontent;

import java.util.List;
import java.util.Objects;

public record BodyListItem(List<BodyInline> content) {

    public BodyListItem {
        Objects.requireNonNull(content, "content must not be null");
        if (content.isEmpty()) {
            throw new IllegalArgumentException("content must not be empty.");
        }
        content = List.copyOf(content);
    }
}
