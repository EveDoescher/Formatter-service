package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.StyleType;
import com.abntbuilder.formatter.profile.model.TextAlignment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record StyleRuleRequest(
        @NotBlank String id,
        @NotNull StyleType type,
        @NotBlank String fontFamily,
        @NotNull @Positive BigDecimal fontSizePt,
        @NotNull TextAlignment alignment,
        @NotNull @Positive BigDecimal lineSpacing,
        @NotNull @PositiveOrZero BigDecimal firstLineIndentCm,
        @NotNull @PositiveOrZero BigDecimal leftIndentCm,
        @NotNull @PositiveOrZero BigDecimal rightIndentCm,
        @NotNull @PositiveOrZero BigDecimal spacingBeforePt,
        @NotNull @PositiveOrZero BigDecimal spacingAfterPt,
        @NotNull Boolean bold,
        @NotNull Boolean italic,
        @NotNull Boolean uppercase
) {
    public StyleRule toDomain() {
        return new StyleRule(
                id,
                type,
                fontFamily,
                fontSizePt,
                alignment,
                lineSpacing,
                firstLineIndentCm,
                leftIndentCm,
                rightIndentCm,
                spacingBeforePt,
                spacingAfterPt,
                bold,
                italic,
                uppercase
        );
    }
}