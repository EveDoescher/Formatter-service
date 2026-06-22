package com.abntbuilder.formatter.output.docx.api;

import java.util.List;
import java.util.Objects;

public record DocxFootnoteReferenceBlock(
        DocxBlock hostBlock,
        List<DocxFootnoteContent> footnotes
) implements DocxBlock {
    public DocxFootnoteReferenceBlock {
        Objects.requireNonNull(hostBlock, "hostBlock must not be null");
        Objects.requireNonNull(footnotes, "footnotes must not be null");
        if (footnotes.isEmpty()) throw new IllegalArgumentException("footnotes must not be empty.");
        footnotes = List.copyOf(footnotes);
    }
}
