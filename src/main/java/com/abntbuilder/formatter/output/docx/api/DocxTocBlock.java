package com.abntbuilder.formatter.output.docx.api;

import com.abntbuilder.formatter.profile.model.StyleRule;

import java.util.Objects;

public record DocxTocBlock(
        StyleRule styleRule,
        String tocInstruction
) implements DocxBlock {
    public DocxTocBlock {
        Objects.requireNonNull(styleRule, "styleRule must not be null");
        if (tocInstruction == null || tocInstruction.isBlank())
            throw new IllegalArgumentException("tocInstruction must not be blank.");
    }
}
