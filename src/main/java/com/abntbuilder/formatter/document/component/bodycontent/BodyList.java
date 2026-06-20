package com.abntbuilder.formatter.document.component.bodycontent;

import java.util.List;
import java.util.Objects;

public record BodyList(
        BodyListType type,
        List<BodyListItem> items
) implements BodyBlock {

    public BodyList {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(items, "items must not be null");
        if (items.isEmpty()) {
            throw new IllegalArgumentException("items must not be empty.");
        }
        items = List.copyOf(items);
    }
}
