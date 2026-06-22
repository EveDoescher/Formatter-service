package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.bodycontent.BodyQuoteMarker;
import com.abntbuilder.formatter.document.component.bodycontent.BodyQuoteMarkerType;
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
