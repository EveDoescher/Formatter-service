package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageItemRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.HorizontalPlacementRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.HorizontalPlacementStrategy;

import java.util.Optional;

public record SinglePageItemRuleRequest(
        String id,
        Boolean required,
        Integer maxVisualLinesPerValue,
        HorizontalPlacementStrategy horizontalPlacement
) {

    public SinglePageItemRule toDomain() {
        return new SinglePageItemRule(
                id,
                Boolean.TRUE.equals(required),
                Optional.ofNullable(maxVisualLinesPerValue),
                new HorizontalPlacementRule(horizontalPlacement == null
                        ? HorizontalPlacementStrategy.FULL_CONTENT_WIDTH
                        : horizontalPlacement)
        );
    }
}
