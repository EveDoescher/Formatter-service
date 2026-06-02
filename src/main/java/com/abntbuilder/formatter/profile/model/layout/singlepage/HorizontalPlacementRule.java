package com.abntbuilder.formatter.profile.model.layout.singlepage;

import java.util.Objects;

public record HorizontalPlacementRule(
        HorizontalPlacementStrategy strategy
) {

    public HorizontalPlacementRule {
        Objects.requireNonNull(strategy, "strategy must not be null");
    }

    public static HorizontalPlacementRule fullContentWidth() {
        return new HorizontalPlacementRule(HorizontalPlacementStrategy.FULL_CONTENT_WIDTH);
    }
}
