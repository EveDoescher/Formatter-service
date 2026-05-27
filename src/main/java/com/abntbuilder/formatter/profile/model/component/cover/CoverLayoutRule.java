package com.abntbuilder.formatter.profile.model.component.cover;

import java.math.BigDecimal;
import java.util.Objects;

public record CoverLayoutRule(
        BigDecimal topToAuthorWeight,
        BigDecimal authorToTitleWeight,
        BigDecimal titleToBottomWeight,
        int maxCharactersPerLine
) {
    public CoverLayoutRule {
        requirePositive(topToAuthorWeight, "topToAuthorWeight");
        requirePositive(authorToTitleWeight, "authorToTitleWeight");
        requirePositive(titleToBottomWeight, "titleToBottomWeight");

        if (maxCharactersPerLine <= 0) {
            throw new IllegalArgumentException("maxCharactersPerLine must be greater than zero.");
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
