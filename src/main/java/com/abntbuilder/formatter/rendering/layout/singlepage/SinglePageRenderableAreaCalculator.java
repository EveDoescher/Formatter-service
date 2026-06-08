package com.abntbuilder.formatter.rendering.layout.singlepage;

import com.abntbuilder.formatter.profile.model.PageRule;
import com.abntbuilder.formatter.shared.measurement.MeasurementConverter;

import java.util.Objects;

public final class SinglePageRenderableAreaCalculator {

    public int calculateSafeLineCapacity(PageRule pageRule, int lineHeightTwips) {
        return calculate(pageRule, lineHeightTwips).safeLineCapacity();
    }

    public SinglePageRenderableArea calculate(PageRule pageRule, int lineHeightTwips) {
        Objects.requireNonNull(pageRule, "pageRule must not be null");

        if (lineHeightTwips <= 0) {
            throw new IllegalArgumentException("lineHeightTwips must be greater than zero.");
        }

        int physicalLineCapacity = calculatePhysicalLineCapacity(pageRule, lineHeightTwips);
        int boundarySafetyLineCount = calculateBoundarySafetyLineCount(pageRule, lineHeightTwips);
        int safeLineCapacity = Math.max(physicalLineCapacity - boundarySafetyLineCount, 0);
        int physicalHeightTwips = calculatePhysicalHeightTwips(pageRule);
        int boundarySafetyHeightTwips = calculateBoundarySafetyHeightTwips(pageRule);
        int safeHeightTwips = Math.max(physicalHeightTwips - boundarySafetyHeightTwips, 0);

        return new SinglePageRenderableArea(
                physicalLineCapacity,
                boundarySafetyLineCount,
                safeLineCapacity,
                physicalHeightTwips,
                boundarySafetyHeightTwips,
                safeHeightTwips
        );
    }

    int calculatePhysicalLineCapacity(PageRule pageRule, int lineHeightTwips) {
        Objects.requireNonNull(pageRule, "pageRule must not be null");

        if (lineHeightTwips <= 0) {
            throw new IllegalArgumentException("lineHeightTwips must be greater than zero.");
        }

        return calculatePhysicalHeightTwips(pageRule) / lineHeightTwips;
    }

    int calculateBoundarySafetyLineCount(PageRule pageRule, int lineHeightTwips) {
        Objects.requireNonNull(pageRule, "pageRule must not be null");

        if (lineHeightTwips <= 0) {
            throw new IllegalArgumentException("lineHeightTwips must be greater than zero.");
        }

        return calculateBoundarySafetyHeightTwips(pageRule) / lineHeightTwips;
    }

    int calculatePhysicalHeightTwips(PageRule pageRule) {
        Objects.requireNonNull(pageRule, "pageRule must not be null");

        return MeasurementConverter.centimetersToTwips(pageRule.usableHeightCm());
    }

    int calculateBoundarySafetyHeightTwips(PageRule pageRule) {
        Objects.requireNonNull(pageRule, "pageRule must not be null");

        return MeasurementConverter.centimetersToTwips(
                pageRule.marginTopCm().add(pageRule.marginBottomCm())
        );
    }
}
