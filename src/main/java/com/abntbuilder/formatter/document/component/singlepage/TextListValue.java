package com.abntbuilder.formatter.document.component.singlepage;

import java.util.List;
import java.util.Objects;

public record TextListValue(List<String> items) implements ContentValue {

    public TextListValue {
        Objects.requireNonNull(items, "TextListValue.items must not be null.");
        if (items.isEmpty()) {
            throw new IllegalArgumentException("TextListValue.items must not be empty.");
        }
        for (String item : items) {
            if (item == null || item.isBlank()) {
                throw new IllegalArgumentException("TextListValue.items must not contain blank entries.");
            }
        }
        items = List.copyOf(items);
    }
}
