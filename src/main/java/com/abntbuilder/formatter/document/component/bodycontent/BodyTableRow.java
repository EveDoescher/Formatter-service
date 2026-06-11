package com.abntbuilder.formatter.document.component.bodycontent;

import java.util.List;
import java.util.Objects;

public record BodyTableRow(
        List<String> cells
) {

    public BodyTableRow {
        Objects.requireNonNull(cells, "cells must not be null");
        if (cells.isEmpty()) {
            throw new IllegalArgumentException("cells must not be empty.");
        }

        cells = List.copyOf(cells);

        for (String cell : cells) {
            if (cell == null) {
                throw new IllegalArgumentException("cells must not contain null values.");
            }
        }
    }
}
