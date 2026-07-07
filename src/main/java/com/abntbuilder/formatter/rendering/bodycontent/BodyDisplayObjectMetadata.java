package com.abntbuilder.formatter.rendering.bodycontent;

public record BodyDisplayObjectMetadata(
        String id,
        int number,
        String caption
) {
    public BodyDisplayObjectMetadata {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id must not be blank.");
        if (number < 1) throw new IllegalArgumentException("number must be >= 1.");
        if (caption == null || caption.isBlank()) throw new IllegalArgumentException("caption must not be blank.");
    }
}
