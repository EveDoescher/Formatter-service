package com.abntbuilder.formatter.rendering.bodycontent;

public record BodyDisplayObjectMetadata(
        String id,
        String number,
        String caption
) {
    public BodyDisplayObjectMetadata {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id must not be blank.");
        if (number == null || number.isBlank()) throw new IllegalArgumentException("number must not be blank.");
        if (caption == null || caption.isBlank()) throw new IllegalArgumentException("caption must not be blank.");
    }

    public BodyDisplayObjectMetadata(String id, int number, String caption) {
        this(id, String.valueOf(number), caption);
    }

    public String bookmarkName() {
        return "elem_" + id;
    }
}
