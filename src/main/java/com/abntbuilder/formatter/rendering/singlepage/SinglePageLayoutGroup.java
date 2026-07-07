package com.abntbuilder.formatter.rendering.singlepage;

import java.util.List;
import java.util.Objects;

public record SinglePageLayoutGroup(
        String id,
        List<SinglePageLayoutItem> items
) {

    public SinglePageLayoutGroup {
        requireNonBlank(id, "id");
        Objects.requireNonNull(items, "items must not be null");

        if (items.isEmpty()) {
            throw new IllegalArgumentException("items must not be empty.");
        }

        items = List.copyOf(items);

        for (SinglePageLayoutItem item : items) {
            Objects.requireNonNull(item, "items must not contain null values.");
        }
    }

    public int lineCount() {
        return items.stream()
                .mapToInt(SinglePageLayoutItem::lineCount)
                .sum();
    }

    public SinglePageLayoutItem firstItem() {
        return items.getFirst();
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
