package com.abntbuilder.formatter.document.component.bodycontent;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record BodyTable(
        String id,
        Optional<String> continuationGroupId,
        String caption,
        Optional<String> source,
        List<BodyTableColumn> columns,
        List<BodyTableRow> rows
) implements NumberedDisplayObject {

    public BodyTable {
        requireNonBlank(id, "id");
        Objects.requireNonNull(continuationGroupId, "continuationGroupId must not be null");
        requireNonBlank(caption, "caption");
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(columns, "columns must not be null");
        Objects.requireNonNull(rows, "rows must not be null");

        if (columns.isEmpty()) {
            throw new IllegalArgumentException("columns must not be empty.");
        }

        if (rows.isEmpty()) {
            throw new IllegalArgumentException("rows must not be empty.");
        }

        continuationGroupId.ifPresent(value -> requireNonBlank(value, "continuationGroupId"));
        source.ifPresent(value -> requireNonBlank(value, "source"));

        columns = List.copyOf(columns);
        rows = List.copyOf(rows);

        int columnCount = columns.size();
        for (BodyTableRow row : rows) {
            if (row.effectiveColumnCount() != columnCount) {
                throw new IllegalArgumentException("table row effective column count must match column count.");
            }
        }
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
