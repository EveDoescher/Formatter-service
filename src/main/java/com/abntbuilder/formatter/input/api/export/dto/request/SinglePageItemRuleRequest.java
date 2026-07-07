package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.profile.layout.singlepage.SinglePageItemRule;
import com.abntbuilder.formatter.engine.model.profile.layout.singlepage.HorizontalPlacementRule;
import com.abntbuilder.formatter.engine.model.profile.layout.singlepage.HorizontalPlacementStrategy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Optional;
import java.util.Objects;

public record SinglePageItemRuleRequest(
        @NotBlank String id,
        @NotNull Boolean required,
        Integer maxVisualLinesPerValue,
        @NotNull
        HorizontalPlacementStrategy horizontalPlacement,
        Integer blankLinesAfter
) {

    public SinglePageItemRule toDomain() {
        return new SinglePageItemRule(
                id,
                Objects.requireNonNull(required, "required must not be null"),
                Optional.ofNullable(maxVisualLinesPerValue),
                new HorizontalPlacementRule(Objects.requireNonNull(
                        horizontalPlacement,
                        "horizontalPlacement must not be null"
                )),
                blankLinesAfter == null ? 0 : blankLinesAfter
        );
    }
}
