package com.abntbuilder.formatter.engine.model.output;

import com.abntbuilder.formatter.engine.model.profile.StyleRule;

import java.util.List;
import java.util.Objects;

public record DocxTocBlock(
        StyleRule styleRule,
        String tocInstruction,
        List<StyleRule> entryStylesByLevel,
        double contentWidthCm
) implements DocxBlock {
    public DocxTocBlock {
        Objects.requireNonNull(styleRule, "styleRule must not be null");
        if (tocInstruction == null || tocInstruction.isBlank())
            throw new IllegalArgumentException("tocInstruction must not be blank.");
        Objects.requireNonNull(entryStylesByLevel, "entryStylesByLevel must not be null");
        if (entryStylesByLevel.isEmpty())
            throw new IllegalArgumentException("entryStylesByLevel must not be empty.");
        entryStylesByLevel = List.copyOf(entryStylesByLevel);
        if (contentWidthCm <= 0)
            throw new IllegalArgumentException("contentWidthCm must be > 0.");
    }
}
