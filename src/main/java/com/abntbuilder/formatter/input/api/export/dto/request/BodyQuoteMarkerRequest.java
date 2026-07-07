package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyQuoteMarker;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyQuoteMarkerType;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;

public record BodyQuoteMarkerRequest(
        @NotNull BodyQuoteMarkerType type,
        int position,
        Integer endPosition
) {
    public BodyQuoteMarker toDomain() {
        return new BodyQuoteMarker(
                type,
                position,
                Optional.ofNullable(endPosition)
        );
    }
}
