package com.abntbuilder.formatter.rendering.layout.singlepage;

import com.abntbuilder.formatter.profile.model.PageRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.HorizontalPlacementRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.HorizontalPlacementStrategy;
import com.abntbuilder.formatter.rendering.layout.text.TextMeasurementArea;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class HorizontalPlacementResolver {

    public TextMeasurementArea resolve(
            PageRule pageRule,
            HorizontalPlacementRule rule
    ) {
        Objects.requireNonNull(pageRule, "pageRule must not be null");
        Objects.requireNonNull(rule, "rule must not be null");

        return switch (rule.strategy()) {
            case FULL_CONTENT_WIDTH -> TextMeasurementArea.fullContentWidth(pageRule);
            case FROM_PAGE_CENTER_TO_RIGHT_MARGIN -> resolveFromPageCenterToRightMargin(pageRule);
        };
    }

    private static TextMeasurementArea resolveFromPageCenterToRightMargin(PageRule pageRule) {
        BigDecimal usableWidthCm = pageRule.usableWidthCm();
        BigDecimal leftIndentCm = usableWidthCm.divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP);
        BigDecimal availableWidthCm = usableWidthCm.subtract(leftIndentCm);

        return new TextMeasurementArea(
                availableWidthCm,
                leftIndentCm,
                BigDecimal.ZERO
        );
    }
}
