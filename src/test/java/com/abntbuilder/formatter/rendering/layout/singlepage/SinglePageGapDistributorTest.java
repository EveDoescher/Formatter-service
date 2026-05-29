package com.abntbuilder.formatter.rendering.layout.singlepage;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SinglePageGapDistributorTest {

    private final SinglePageGapDistributor distributor = new SinglePageGapDistributor();

    @Test
    void shouldReserveOneLineForEachGapWhenThereIsEnoughSpace() {
        int[] gaps = distributor.distribute(
                10,
                List.of(
                        BigDecimal.valueOf(30),
                        BigDecimal.valueOf(10),
                        BigDecimal.valueOf(60)
                )
        );

        assertArrayEquals(new int[]{3, 1, 6}, gaps);
    }

    @Test
    void shouldPreserveTotalAvailableGapLines() {
        int[] gaps = distributor.distribute(
                7,
                List.of(
                        BigDecimal.valueOf(1),
                        BigDecimal.valueOf(1),
                        BigDecimal.valueOf(1)
                )
        );

        assertEquals(7, gaps[0] + gaps[1] + gaps[2]);
    }

    @Test
    void shouldAllowZeroLineGapsWhenThereIsNotEnoughSpaceForEveryGap() {
        int[] gaps = distributor.distribute(
                2,
                List.of(
                        BigDecimal.valueOf(30),
                        BigDecimal.valueOf(10),
                        BigDecimal.valueOf(60)
                )
        );

        assertEquals(2, gaps[0] + gaps[1] + gaps[2]);
    }

    @Test
    void shouldRejectAvailableLinesWithoutGaps() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> distributor.distribute(1, List.of())
        );

        assertEquals("Cannot distribute gap lines without gaps.", exception.getMessage());
    }

    @Test
    void shouldRejectNonPositiveWeights() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> distributor.distribute(1, List.of(BigDecimal.ZERO))
        );

        assertEquals("gapWeights must contain only positive values.", exception.getMessage());
    }
}
