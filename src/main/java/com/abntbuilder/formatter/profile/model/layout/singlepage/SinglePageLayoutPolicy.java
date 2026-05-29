package com.abntbuilder.formatter.profile.model.layout.singlepage;

import java.util.Objects;

public record SinglePageLayoutPolicy(
        SinglePageAnchorStrategy anchorStrategy,
        SinglePageLineHeightStrategy lineHeightStrategy,
        SpacerStylePolicy spacerStylePolicy,
        SinglePageSafetyPolicyId safetyPolicy
) {

    public SinglePageLayoutPolicy {
        Objects.requireNonNull(anchorStrategy, "anchorStrategy must not be null");
        Objects.requireNonNull(lineHeightStrategy, "lineHeightStrategy must not be null");
        Objects.requireNonNull(spacerStylePolicy, "spacerStylePolicy must not be null");
        Objects.requireNonNull(safetyPolicy, "safetyPolicy must not be null");
    }

    public static SinglePageLayoutPolicy defaultSinglePagePolicy() {
        return new SinglePageLayoutPolicy(
                SinglePageAnchorStrategy.LAST_GROUP_AT_SAFE_AREA_END,
                SinglePageLineHeightStrategy.MAX_EXACT_LINE_HEIGHT,
                SpacerStylePolicy.NEXT_GROUP_STYLE,
                SinglePageSafetyPolicyId.MARGIN_BASED
        );
    }
}
