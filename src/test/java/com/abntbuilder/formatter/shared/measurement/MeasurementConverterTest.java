package com.abntbuilder.formatter.shared.measurement;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MeasurementConverterTest {

    @Test
    void shouldConvertCentimetersToTwips() {
        assertEquals(1440, MeasurementConverter.centimetersToTwips(BigDecimal.valueOf(2.54)));
        assertEquals(567, MeasurementConverter.centimetersToTwips(BigDecimal.ONE));
    }

    @Test
    void shouldConvertPointsToTwips() {
        assertEquals(240, MeasurementConverter.pointsToTwips(BigDecimal.valueOf(12)));
        assertEquals(200, MeasurementConverter.pointsToTwips(BigDecimal.valueOf(10)));
    }

    @Test
    void shouldConvertPointsToHalfPoints() {
        assertEquals(24, MeasurementConverter.pointsToHalfPoints(BigDecimal.valueOf(12)));
        assertEquals(20, MeasurementConverter.pointsToHalfPoints(BigDecimal.valueOf(10)));
    }

    @Test
    void shouldRejectNullCentimeters() {
        assertThrows(NullPointerException.class, () -> MeasurementConverter.centimetersToTwips(null));
    }

    @Test
    void shouldRejectNullPointsForTwips() {
        assertThrows(NullPointerException.class, () -> MeasurementConverter.pointsToTwips(null));
    }

    @Test
    void shouldRejectNullPointsForHalfPoints() {
        assertThrows(NullPointerException.class, () -> MeasurementConverter.pointsToHalfPoints(null));
    }

    @Test
    void shouldConvertLineSpacingMultiplierToDocxLineValue() {
        assertEquals(240, MeasurementConverter.lineSpacingMultiplierToDocxLineValue(BigDecimal.ONE));
        assertEquals(360, MeasurementConverter.lineSpacingMultiplierToDocxLineValue(BigDecimal.valueOf(1.5)));
        assertEquals(480, MeasurementConverter.lineSpacingMultiplierToDocxLineValue(BigDecimal.valueOf(2)));
    }

    @Test
    void shouldRejectZeroLineSpacingMultiplier() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> MeasurementConverter.lineSpacingMultiplierToDocxLineValue(BigDecimal.ZERO)
        );

        assertEquals("lineSpacing must be greater than zero.", exception.getMessage());
    }

    @Test
    void shouldRejectNullLineSpacingMultiplier() {
        assertThrows(
                NullPointerException.class,
                () -> MeasurementConverter.lineSpacingMultiplierToDocxLineValue(null)
        );
    }

    @Test
    void shouldConvertPointsToCentimeters() {
        assertEquals(0, BigDecimal.valueOf(2.54).compareTo(
                MeasurementConverter.pointsToCentimeters(BigDecimal.valueOf(72))
        ));

        assertEquals(0, BigDecimal.valueOf(1.27).compareTo(
                MeasurementConverter.pointsToCentimeters(BigDecimal.valueOf(36)).stripTrailingZeros()
        ));
    }

    @Test
    void shouldRejectNullPointsForCentimeters() {
        assertThrows(NullPointerException.class, () -> MeasurementConverter.pointsToCentimeters(null));
    }

    @Test
    void shouldConvertCentimetersToPoints() {
        assertEquals(0, BigDecimal.valueOf(72).compareTo(
                MeasurementConverter.centimetersToPoints(BigDecimal.valueOf(2.54))
        ));

        assertEquals(0, BigDecimal.valueOf(36).compareTo(
                MeasurementConverter.centimetersToPoints(BigDecimal.valueOf(1.27)).stripTrailingZeros()
        ));
    }

    @Test
    void shouldRejectNullCentimetersForPoints() {
        assertThrows(NullPointerException.class, () -> MeasurementConverter.centimetersToPoints(null));
    }
}