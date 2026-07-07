package com.abntbuilder.formatter.engine.model.profile.layout.singlepage;

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
