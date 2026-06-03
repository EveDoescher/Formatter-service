package com.abntbuilder.formatter.rendering.layout.singlepage;

import com.abntbuilder.formatter.profile.model.PageRule;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.HorizontalPlacementRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.HorizontalPlacementStrategy;
import com.abntbuilder.formatter.rendering.layout.text.TextMeasurementArea;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class HorizontalPlacementResolver {

    public TextMeasurementArea resolve(
            PageRule pageRule,
            StyleRule styleRule,
            HorizontalPlacementRule rule
    ) {
        Objects.requireNonNull(pageRule, "pageRule must not be null");
        Objects.requireNonNull(styleRule, "styleRule must not be null");
        Objects.requireNonNull(rule, "rule must not be null");

        return switch (rule.strategy()) {
            case FULL_CONTENT_WIDTH -> TextMeasurementArea.fromStyle(pageRule, styleRule);
            case FROM_PAGE_CENTER_TO_RIGHT_MARGIN -> resolveFromPageCenterToRightMargin(pageRule, styleRule);
        };
    }

    private static TextMeasurementArea resolveFromPageCenterToRightMargin(PageRule pageRule, StyleRule styleRule) {
        BigDecimal usableWidthCm = pageRule.usableWidthCm();
        BigDecimal baseLeftIndentCm = usableWidthCm.divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP);
        BigDecimal leftIndentCm = baseLeftIndentCm.add(styleRule.leftIndentCm());
        BigDecimal rightIndentCm = styleRule.rightIndentCm();
        BigDecimal availableWidthCm = usableWidthCm
                .subtract(leftIndentCm)
                .subtract(rightIndentCm);

        return new TextMeasurementArea(
                availableWidthCm,
                leftIndentCm,
                rightIndentCm
        );
    }
}
