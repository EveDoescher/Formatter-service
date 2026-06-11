package com.abntbuilder.formatter.document.component.bodycontent;

import java.util.Objects;
import java.util.Optional;

public record BodyFigure(
        String id,
        Optional<String> continuationGroupId,
        String caption,
        Optional<String> source,
        BodyImageSource image
) implements BodyBlock {

    public BodyFigure {
        requireNonBlank(id, "id");
        Objects.requireNonNull(continuationGroupId, "continuationGroupId must not be null");
        requireNonBlank(caption, "caption");
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(image, "image must not be null");

        continuationGroupId.ifPresent(value -> requireNonBlank(value, "continuationGroupId"));
        source.ifPresent(value -> requireNonBlank(value, "source"));
    }

    public String displayGroupKey() {
        return continuationGroupId.orElse(id);
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
