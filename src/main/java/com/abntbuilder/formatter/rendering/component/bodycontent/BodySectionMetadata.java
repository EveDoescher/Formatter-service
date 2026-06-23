package com.abntbuilder.formatter.rendering.component.bodycontent;

public record BodySectionMetadata(
        String id,
        int level,
        String renderedTitle,
        String renderedNumber
) {
    public BodySectionMetadata {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id must not be blank.");
        if (level < 1 || level > 6) throw new IllegalArgumentException("level must be between 1 and 6.");
        if (renderedTitle == null || renderedTitle.isBlank()) throw new IllegalArgumentException("renderedTitle must not be blank.");
        if (renderedNumber == null || renderedNumber.isBlank()) throw new IllegalArgumentException("renderedNumber must not be blank.");
    }
}
