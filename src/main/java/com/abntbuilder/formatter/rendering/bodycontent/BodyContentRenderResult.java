package com.abntbuilder.formatter.rendering.bodycontent;

import com.abntbuilder.formatter.engine.model.output.DocxBlock;
import com.abntbuilder.formatter.engine.contract.ComponentRenderResult;

import java.util.List;
import java.util.Objects;

public record BodyContentRenderResult(
        List<DocxBlock> blocks,
        BodyContentMetadata metadata
) implements ComponentRenderResult {
    public BodyContentRenderResult {
        Objects.requireNonNull(blocks, "blocks must not be null");
        Objects.requireNonNull(metadata, "metadata must not be null");
        blocks = List.copyOf(blocks);
    }
}
