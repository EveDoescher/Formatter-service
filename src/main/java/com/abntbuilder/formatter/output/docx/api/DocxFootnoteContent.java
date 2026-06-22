package com.abntbuilder.formatter.output.docx.api;

import java.util.List;
import java.util.Objects;

public record DocxFootnoteContent(int id, List<DocxRun> contentRuns) {
    public DocxFootnoteContent {
        if (id < 1) throw new IllegalArgumentException("id must be >= 1.");
        Objects.requireNonNull(contentRuns, "contentRuns must not be null");
        if (contentRuns.isEmpty()) throw new IllegalArgumentException("contentRuns must not be empty.");
        contentRuns = List.copyOf(contentRuns);
    }
}
