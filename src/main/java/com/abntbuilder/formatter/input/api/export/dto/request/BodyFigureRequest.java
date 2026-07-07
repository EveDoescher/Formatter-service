package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyFigure;
import jakarta.validation.Valid;

import java.util.Optional;

public record BodyFigureRequest(
        String id,
        String continuationGroupId,
        String caption,
        String source,
        @Valid ImageSourceRequest image
) {

    BodyFigure toDomain() {
        if (image == null) {
            throw new IllegalArgumentException("figure.image must be provided.");
        }

        return new BodyFigure(
                id,
                Optional.ofNullable(continuationGroupId),
                caption,
                Optional.ofNullable(source),
                image.toDomain()
        );
    }
}
