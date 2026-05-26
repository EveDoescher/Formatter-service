package com.abntbuilder.formatter.output.docx.api;

import com.abntbuilder.formatter.profile.model.PageRule;

import java.util.List;
import java.util.Objects;

public record DocxDocument(
        PageRule pageRule,
        List<DocxBlock> blocks
) {
    public DocxDocument {
        Objects.requireNonNull(pageRule, "pageRule must not be null");
        Objects.requireNonNull(blocks, "blocks must not be null");

        if (blocks.isEmpty()) {
            throw new IllegalArgumentException("blocks must not be empty.");
        }

        blocks = List.copyOf(blocks);

        for (DocxBlock block : blocks) {
            Objects.requireNonNull(block, "blocks must not contain null values.");
        }
    }
}