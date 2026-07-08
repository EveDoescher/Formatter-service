package com.abntbuilder.formatter.engine.model.output;

import com.abntbuilder.formatter.engine.model.profile.StyleRule;

import java.math.BigDecimal;
import java.util.Objects;

public record DocxIndexEntryParagraph(
        String entryText,
        String bookmarkName,
        StyleRule styleRule,
        BigDecimal contentWidthCm
) implements DocxBlock {

    public DocxIndexEntryParagraph {
        if (entryText == null || entryText.isBlank())
            throw new IllegalArgumentException("entryText must not be blank.");
        if (bookmarkName == null || bookmarkName.isBlank())
            throw new IllegalArgumentException("bookmarkName must not be blank.");
        Objects.requireNonNull(styleRule, "styleRule must not be null");
        Objects.requireNonNull(contentWidthCm, "contentWidthCm must not be null");
        if (contentWidthCm.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("contentWidthCm must be greater than zero.");
    }
}
