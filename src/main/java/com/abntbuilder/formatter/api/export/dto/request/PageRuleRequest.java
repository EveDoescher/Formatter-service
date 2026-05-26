package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.PageOrientation;
import com.abntbuilder.formatter.profile.model.PageRule;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record PageRuleRequest(
        @NotNull @Positive BigDecimal widthCm,
        @NotNull @Positive BigDecimal heightCm,
        @NotNull @PositiveOrZero BigDecimal marginTopCm,
        @NotNull @PositiveOrZero BigDecimal marginRightCm,
        @NotNull @PositiveOrZero BigDecimal marginBottomCm,
        @NotNull @PositiveOrZero BigDecimal marginLeftCm,
        @NotNull PageOrientation orientation
) {
    public PageRule toDomain() {
        return new PageRule(
                widthCm,
                heightCm,
                marginTopCm,
                marginRightCm,
                marginBottomCm,
                marginLeftCm,
                orientation
        );
    }
}