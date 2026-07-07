package com.abntbuilder.formatter.engine.model.content.bodycontent;

import java.util.Objects;
import java.util.Optional;

public record BodyQuoteMarker(
        BodyQuoteMarkerType type,
        int position,
        Optional<Integer> endPosition
) {
    public BodyQuoteMarker {
        Objects.requireNonNull(type, "type must not be null");
        if (position < 0) throw new IllegalArgumentException("position must be >= 0.");
        Objects.requireNonNull(endPosition, "endPosition must not be null");
        if (type == BodyQuoteMarkerType.INTERPOLATION && endPosition.isEmpty()) {
            throw new IllegalArgumentException("INTERPOLATION marker requires endPosition.");
        }
    }

    public static BodyQuoteMarker suppression(int position) {
        return new BodyQuoteMarker(BodyQuoteMarkerType.SUPPRESSION, position, Optional.empty());
    }

    public static BodyQuoteMarker emphasisOurs() {
        return new BodyQuoteMarker(BodyQuoteMarkerType.EMPHASIS_OURS, 0, Optional.empty());
    }

    public static BodyQuoteMarker emphasisAuthor() {
        return new BodyQuoteMarker(BodyQuoteMarkerType.EMPHASIS_AUTHOR, 0, Optional.empty());
    }
}
