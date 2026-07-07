package com.abntbuilder.formatter.rendering.singlepage;

import com.abntbuilder.formatter.engine.model.profile.PageOrientation;
import com.abntbuilder.formatter.engine.model.profile.PageRule;
import com.abntbuilder.formatter.shared.measurement.MeasurementConverter;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class SinglePageRenderableAreaCalculatorTest {

    private final SinglePageRenderableAreaCalculator calculator = new SinglePageRenderableAreaCalculator();

    @Test
    void shouldCalculateSafeLineCapacityFromPhysicalCapacityAndBoundarySafetyLines() {
        int lineHeightTwips = MeasurementConverter.pointsToTwips(BigDecimal.valueOf(18));

        SinglePageRenderableArea area = calculator.calculate(validPageRule(), lineHeightTwips);

        assertTrue(area.physicalLineCapacity() > 0);
        assertTrue(area.boundarySafetyLineCount() > 0);
        assertEquals(
                area.physicalLineCapacity() - area.boundarySafetyLineCount(),
                area.safeLineCapacity()
        );
    }

    @Test
    void shouldCalculateExpectedSafeCapacityForAbntA4WithTwelvePointOneAndHalfSpacing() {
        int lineHeightTwips = MeasurementConverter.pointsToTwips(BigDecimal.valueOf(18));
        SinglePageRenderableArea area = calculator.calculate(validPageRule(), lineHeightTwips);

        assertEquals(38, area.physicalLineCapacity());
        assertEquals(7, area.boundarySafetyLineCount());
        assertEquals(31, area.safeLineCapacity());
        assertEquals(31, calculator.calculateSafeLineCapacity(validPageRule(), lineHeightTwips));
    }

    @Test
    void shouldRejectNullPageRule() {
        assertThrows(
                NullPointerException.class,
                () -> calculator.calculateSafeLineCapacity(null, 360)
        );
    }

    @Test
    void shouldRejectNonPositiveLineHeight() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculateSafeLineCapacity(validPageRule(), 0)
        );

        assertEquals("lineHeightTwips must be greater than zero.", exception.getMessage());
    }

    @Test
    void shouldReturnZeroWhenBoundarySafetyConsumesPhysicalCapacity() {
        PageRule pageRule = new PageRule(
                BigDecimal.valueOf(21),
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(4),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(4),
                BigDecimal.valueOf(3),
                PageOrientation.PORTRAIT
        );

        SinglePageRenderableArea area = calculator.calculate(pageRule, 360);

        assertEquals(0, area.safeLineCapacity());
        assertEquals(0, calculator.calculateSafeLineCapacity(pageRule, 360));
    }

    @Test
    void shouldRejectRenderableAreaWithInvalidCounts() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new SinglePageRenderableArea(-1, 0, 0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new SinglePageRenderableArea(1, -1, 0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new SinglePageRenderableArea(1, 0, -1)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new SinglePageRenderableArea(1, 0, 2)
        );
    }

    private static PageRule validPageRule() {
        return new PageRule(
                BigDecimal.valueOf(21),
                BigDecimal.valueOf(29.7),
                BigDecimal.valueOf(3),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(3),
                PageOrientation.PORTRAIT
        );
    }
}
