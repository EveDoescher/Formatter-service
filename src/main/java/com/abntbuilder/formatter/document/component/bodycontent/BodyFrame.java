package com.abntbuilder.formatter.document.component.bodycontent;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record BodyFrame(
        String id,
        Optional<String> continuationGroupId,
        String caption,
        Optional<String> source,
        List<BodyTableColumn> columns,
        List<BodyTableRow> rows
) implements NumberedDisplayObject {

    public BodyFrame {
        requireNonBlank(id, "id");
        Objects.requireNonNull(continuationGroupId, "continuationGroupId must not be null");
        requireNonBlank(caption, "caption");
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(columns, "columns must not be null");
        if (columns.isEmpty()) throw new IllegalArgumentException("columns must not be empty.");
        Objects.requireNonNull(rows, "rows must not be null");
        if (rows.isEmpty()) throw new IllegalArgumentException("rows must not be empty.");
        int colCount = columns.size();
        for (BodyTableRow row : rows) {
            if (row.cells().size() != colCount) {
                throw new IllegalArgumentException(
                        "frame row cell count must match column count (" + colCount + ")."
                );
            }
        }
        columns = List.copyOf(columns);
        rows = List.copyOf(rows);
    }

    private static void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank.");
        }
    }
}
