package com.abntbuilder.formatter.document.component.bodycontent;

import java.util.Objects;
import java.util.Optional;

public record BodyCodeListing(
        String id,
        Optional<String> continuationGroupId,
        String caption,
        Optional<String> language,
        String code,
        Optional<String> source
) implements NumberedDisplayObject {

    public BodyCodeListing {
        requireNonBlank(id, "id");
        Objects.requireNonNull(continuationGroupId, "continuationGroupId must not be null");
        requireNonBlank(caption, "caption");
        Objects.requireNonNull(language, "language must not be null");
        requireNonBlank(code, "code");
        Objects.requireNonNull(source, "source must not be null");
    }

    private static void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank.");
        }
    }
}
