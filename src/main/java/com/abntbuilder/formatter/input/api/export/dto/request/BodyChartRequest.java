package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyChart;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Optional;

public record BodyChartRequest(
        @NotBlank String id,
        String continuationGroupId,
        @NotBlank String caption,
        String source,
        @Valid @NotNull ImageSourceRequest image
) {
    public BodyChart toDomain() {
        return new BodyChart(
                id,
                Optional.ofNullable(continuationGroupId),
                caption,
                Optional.ofNullable(source),
                image.toDomain()
        );
    }
}
