package com.abntbuilder.formatter.profile.model.component.cover;

import java.math.BigDecimal;
import java.util.Objects;

public record CoverLayoutRule(
        BigDecimal topToAuthorWeight,
        BigDecimal authorToTitleWeight,
        BigDecimal titleToBottomWeight,
        int safetyBlankLines
) {
    public CoverLayoutRule {
        requirePositive(topToAuthorWeight, "topToAuthorWeight");
        requirePositive(authorToTitleWeight, "authorToTitleWeight");
        requirePositive(titleToBottomWeight, "titleToBottomWeight");

        if (safetyBlankLines < 0) {
            throw new IllegalArgumentException("safetyBlankLines must not be negative.");
        }
    }

    public BigDecimal totalWeight() {
        return topToAuthorWeight
                .add(authorToTitleWeight)
                .add(titleToBottomWeight);
    }

    private static void requirePositive(BigDecimal value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");

        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than zero.");
        }
    }
}