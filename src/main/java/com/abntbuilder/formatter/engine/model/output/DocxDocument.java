package com.abntbuilder.formatter.engine.model.output;

import com.abntbuilder.formatter.engine.model.profile.PageRule;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record DocxDocument(
        PageRule pageRule,
        Optional<DocxPageNumbering> initialPageNumbering,
        List<DocxBlock> blocks
) {

    public DocxDocument(PageRule pageRule, List<DocxBlock> blocks) {
        this(pageRule, Optional.empty(), blocks);
    }

    public DocxDocument {
        Objects.requireNonNull(pageRule, "pageRule must not be null");
        Objects.requireNonNull(initialPageNumbering, "initialPageNumbering must not be null");
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
