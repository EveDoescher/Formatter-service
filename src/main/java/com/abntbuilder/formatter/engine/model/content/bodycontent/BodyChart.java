package com.abntbuilder.formatter.engine.model.content.bodycontent;

import java.util.Objects;
import java.util.Optional;

public record BodyChart(
        String id,
        Optional<String> continuationGroupId,
        String caption,
        Optional<String> source,
        BodyImageSource image
) implements NumberedDisplayObject {

    public BodyChart {
        requireNonBlank(id, "id");
        Objects.requireNonNull(continuationGroupId, "continuationGroupId must not be null");
        requireNonBlank(caption, "caption");
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(image, "image must not be null");
    }

    private static void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank.");
        }
    }
}
