package com.abntbuilder.formatter.rendering.layout.singlepage;

import com.abntbuilder.formatter.profile.model.PageRule;
import com.abntbuilder.formatter.shared.measurement.MeasurementConverter;

import java.util.Objects;

public final class SinglePageRenderableAreaCalculator {

    public int calculateSafeLineCapacity(PageRule pageRule, int lineHeightTwips) {
        Objects.requireNonNull(pageRule, "pageRule must not be null");

        if (lineHeightTwips <= 0) {
            throw new IllegalArgumentException("lineHeightTwips must be greater than zero.");
        }

        int usableHeightTwips = MeasurementConverter.centimetersToTwips(pageRule.usableHeightCm());
        int safeRenderableHeightTwips = usableHeightTwips - calculateSafetyGuardTwips(pageRule);

        if (safeRenderableHeightTwips <= 0) {
            return 0;
        }

        return safeRenderableHeightTwips / lineHeightTwips;
    }

    private static int calculateSafetyGuardTwips(PageRule pageRule) {
        return MeasurementConverter.centimetersToTwips(
                pageRule.marginTopCm().add(pageRule.marginBottomCm())
        );
    }
}
