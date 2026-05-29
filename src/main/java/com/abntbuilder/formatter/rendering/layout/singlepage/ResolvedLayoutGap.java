package com.abntbuilder.formatter.rendering.layout.singlepage;

import com.abntbuilder.formatter.profile.model.layout.singlepage.LayoutGapRule;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public record ResolvedLayoutGap(
        String fromPresentGroupId,
        String toPresentGroupId,
        BigDecimal weight,
        List<LayoutGapRule> sourceGapRules
) {

    public ResolvedLayoutGap {
        requireNonBlank(fromPresentGroupId, "fromPresentGroupId");
        requireNonBlank(toPresentGroupId, "toPresentGroupId");
        Objects.requireNonNull(weight, "weight must not be null");
        Objects.requireNonNull(sourceGapRules, "sourceGapRules must not be null");

        if (weight.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("weight must be greater than zero.");
        }

        if (sourceGapRules.isEmpty()) {
            throw new IllegalArgumentException("sourceGapRules must not be empty.");
        }

        sourceGapRules = List.copyOf(sourceGapRules);
    }

    public String id() {
        return fromPresentGroupId + "->" + toPresentGroupId;
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
