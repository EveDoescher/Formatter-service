package com.abntbuilder.formatter.engine.model.content.bodycontent;

import java.util.Objects;

public record BodyCrossReference(
        String targetId,
        CrossReferenceTargetType targetType,
        CrossReferenceDisplayMode displayMode
) implements BodyInline {

    public BodyCrossReference {
        if (targetId == null || targetId.isBlank()) {
            throw new IllegalArgumentException("targetId must not be blank.");
        }
        Objects.requireNonNull(targetType, "targetType must not be null");
        Objects.requireNonNull(displayMode, "displayMode must not be null");
    }

    @Override
    public String renderedText() {
        return "[ref:" + targetId + "]";
    }
}
