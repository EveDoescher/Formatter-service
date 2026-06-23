package com.abntbuilder.formatter.rendering.component.bodycontent;

import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.rendering.component.ComponentRenderResult;

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
