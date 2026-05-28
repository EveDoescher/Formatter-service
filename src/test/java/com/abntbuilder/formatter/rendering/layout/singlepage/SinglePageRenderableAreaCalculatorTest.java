package com.abntbuilder.formatter.rendering.layout.singlepage;

import com.abntbuilder.formatter.profile.model.PageOrientation;
import com.abntbuilder.formatter.profile.model.PageRule;
import com.abntbuilder.formatter.shared.measurement.MeasurementConverter;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class SinglePageRenderableAreaCalculatorTest {

    private final SinglePageRenderableAreaCalculator calculator = new SinglePageRenderableAreaCalculator();

    @Test
    void shouldCalculateSafeLineCapacityFromPhysicalCapacityAndBoundarySafetyLines() {
        int lineHeightTwips = MeasurementConverter.pointsToTwips(BigDecimal.valueOf(18));

        int physicalCapacity = calculator.calculatePhysicalLineCapacity(validPageRule(), lineHeightTwips);
        int safetyLines = calculator.calculateBoundarySafetyLineCount(validPageRule(), lineHeightTwips);
        int safeCapacity = calculator.calculateSafeLineCapacity(validPageRule(), lineHeightTwips);

        assertTrue(physicalCapacity > 0);
        assertTrue(safetyLines > 0);
        assertEquals(physicalCapacity - safetyLines, safeCapacity);
    }

    @Test
    void shouldCalculateExpectedSafeCapacityForAbntA4WithTwelvePointOneAndHalfSpacing() {
        int lineHeightTwips = MeasurementConverter.pointsToTwips(BigDecimal.valueOf(18));

        assertEquals(38, calculator.calculatePhysicalLineCapacity(validPageRule(), lineHeightTwips));
        assertEquals(7, calculator.calculateBoundarySafetyLineCount(validPageRule(), lineHeightTwips));
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

        assertEquals(0, calculator.calculateSafeLineCapacity(pageRule, 360));
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
