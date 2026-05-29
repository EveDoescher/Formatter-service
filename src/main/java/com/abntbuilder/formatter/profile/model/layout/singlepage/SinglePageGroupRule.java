package com.abntbuilder.formatter.profile.model.layout.singlepage;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record SinglePageGroupRule(
        String id,
        boolean required,
        List<SinglePageItemRule> items
) {

    public SinglePageGroupRule {
        requireNonBlank(id, "id");
        Objects.requireNonNull(items, "items must not be null");

        if (items.isEmpty()) {
            throw new IllegalArgumentException("items must not be empty.");
        }

        items = List.copyOf(items);

        Set<String> itemIds = new HashSet<>();

        for (SinglePageItemRule item : items) {
            Objects.requireNonNull(item, "items must not contain null values.");

            if (!itemIds.add(item.id())) {
                throw new IllegalArgumentException("Duplicate single-page item id: " + item.id());
            }
        }
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
