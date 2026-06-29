package com.abntbuilder.formatter.document.component.bodycontent;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record BodyListItem(List<BodyInline> content, Optional<BodyList> subList) {

    public BodyListItem {
        Objects.requireNonNull(content, "content must not be null");
        if (content.isEmpty()) {
            throw new IllegalArgumentException("content must not be empty.");
        }
        content = List.copyOf(content);
        Objects.requireNonNull(subList, "subList must not be null");
    }

    public BodyListItem(List<BodyInline> content) {
        this(content, Optional.empty());
    }
}
