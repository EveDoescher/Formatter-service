package com.abntbuilder.formatter.profile.model.layout.singlepage;

import java.math.BigDecimal;
import java.util.Objects;

public record LayoutGapRule(
        String fromGroupId,
        String toGroupId,
        BigDecimal weight
) {

    public LayoutGapRule {
        requireNonBlank(fromGroupId, "fromGroupId");
        requireNonBlank(toGroupId, "toGroupId");
        Objects.requireNonNull(weight, "weight must not be null");

        if (weight.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("weight must be greater than zero.");
        }
    }

    public String id() {
        return fromGroupId + "->" + toGroupId;
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
