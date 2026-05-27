package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.cover.CoverLayoutRule;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CoverLayoutRuleRequest(
        @NotNull @Positive BigDecimal topToAuthorWeight,
        @NotNull @Positive BigDecimal authorToTitleWeight,
        @NotNull @Positive BigDecimal titleToBottomWeight,
        @NotNull @Positive Integer maxCharactersPerLine
) {
    public CoverLayoutRule toDomain() {
        return new CoverLayoutRule(
                topToAuthorWeight,
                authorToTitleWeight,
                titleToBottomWeight,
                maxCharactersPerLine
        );
    }
}
