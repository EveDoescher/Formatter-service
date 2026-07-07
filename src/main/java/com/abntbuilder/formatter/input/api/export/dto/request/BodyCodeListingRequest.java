package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyCodeListing;
import jakarta.validation.constraints.NotBlank;

import java.util.Optional;

public record BodyCodeListingRequest(
        @NotBlank String id,
        String continuationGroupId,
        @NotBlank String caption,
        String language,
        @NotBlank String code,
        String source
) {
    public BodyCodeListing toDomain() {
        return new BodyCodeListing(
                id,
                Optional.ofNullable(continuationGroupId),
                caption,
                Optional.ofNullable(language),
                code,
                Optional.ofNullable(source)
        );
    }
}
