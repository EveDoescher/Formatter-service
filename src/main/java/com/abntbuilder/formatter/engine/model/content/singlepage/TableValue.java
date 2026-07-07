package com.abntbuilder.formatter.engine.model.content.singlepage;

import java.util.List;
import java.util.Objects;

public record TableValue(List<List<String>> rows) implements ContentValue {

    public TableValue {
        Objects.requireNonNull(rows, "TableValue.rows must not be null.");
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("TableValue.rows must not be empty.");
        }
        for (List<String> row : rows) {
            Objects.requireNonNull(row, "TableValue.rows must not contain null rows.");
            if (row.isEmpty()) {
                throw new IllegalArgumentException("TableValue.rows must not contain empty rows.");
            }
            rows = rows.stream().map(List::copyOf).toList();
        }
        rows = List.copyOf(rows);
    }
}
