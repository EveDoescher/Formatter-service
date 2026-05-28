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

        int physicalLineCapacity = calculatePhysicalLineCapacity(pageRule, lineHeightTwips);
        int boundarySafetyLineCount = calculateBoundarySafetyLineCount(pageRule, lineHeightTwips);

        return Math.max(physicalLineCapacity - boundarySafetyLineCount, 0);
    }

    int calculatePhysicalLineCapacity(PageRule pageRule, int lineHeightTwips) {
        Objects.requireNonNull(pageRule, "pageRule must not be null");

        if (lineHeightTwips <= 0) {
            throw new IllegalArgumentException("lineHeightTwips must be greater than zero.");
        }

        int usableHeightTwips = MeasurementConverter.centimetersToTwips(pageRule.usableHeightCm());

        return usableHeightTwips / lineHeightTwips;
    }

    int calculateBoundarySafetyLineCount(PageRule pageRule, int lineHeightTwips) {
        Objects.requireNonNull(pageRule, "pageRule must not be null");

        if (lineHeightTwips <= 0) {
            throw new IllegalArgumentException("lineHeightTwips must be greater than zero.");
        }

        int boundarySafetyHeightTwips = MeasurementConverter.centimetersToTwips(
                pageRule.marginTopCm().add(pageRule.marginBottomCm())
        );

        return boundarySafetyHeightTwips / lineHeightTwips;
    }
}
