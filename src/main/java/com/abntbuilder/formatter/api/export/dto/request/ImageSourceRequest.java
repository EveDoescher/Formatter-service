package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.bodycontent.BodyImageSource;
import com.abntbuilder.formatter.document.component.bodycontent.ImageSourceType;
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
