package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyImageSource;
import com.abntbuilder.formatter.engine.model.content.bodycontent.ImageSourceType;
import jakarta.validation.constraints.NotNull;

public record ImageSourceRequest(
        @NotNull ImageSourceType sourceType,
        String dataUri,
        String altText,
        String url
) {

    BodyImageSource toDomain() {
        return new BodyImageSource(sourceType, dataUri, altText, url);
    }
}
