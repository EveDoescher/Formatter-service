package com.abntbuilder.formatter.engine.model.profile;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PageRuleTest {

    @Test
    void shouldCreateValidPageRule() {
        PageRule pageRule = new PageRule(
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(30),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(2),
                PageOrientation.PORTRAIT
        );

        assertEquals(0, BigDecimal.valueOf(16).compareTo(pageRule.usableWidthCm()));
        assertEquals(0, BigDecimal.valueOf(26).compareTo(pageRule.usableHeightCm()));
        assertEquals(PageOrientation.PORTRAIT, pageRule.orientation());
    }

    @Test
    void shouldRejectNullWidth() {
        assertThrows(NullPointerException.class, () -> new PageRule(
                null,
                BigDecimal.valueOf(30),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(2),
                PageOrientation.PORTRAIT
        ));
    }

    @Test
    void shouldRejectZeroWidth() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new PageRule(
                BigDecimal.ZERO,
                BigDecimal.valueOf(30),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(2),
                PageOrientation.PORTRAIT
        ));

        assertEquals("widthCm must be greater than zero.", exception.getMessage());
    }

    @Test
    void shouldRejectNegativeMargin() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new PageRule(
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(30),
                BigDecimal.valueOf(-1),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(2),
                PageOrientation.PORTRAIT
        ));

        assertEquals("marginTopCm must not be negative.", exception.getMessage());
    }

    @Test
    void shouldRejectHorizontalMarginsGreaterThanPageWidth() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new PageRule(
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(30),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(10),
                PageOrientation.PORTRAIT
        ));

        assertEquals("Horizontal margins must be smaller than page width.", exception.getMessage());
    }

    @Test
    void shouldRejectVerticalMarginsGreaterThanPageHeight() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new PageRule(
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(30),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(2),
                PageOrientation.PORTRAIT
        ));

        assertEquals("Vertical margins must be smaller than page height.", exception.getMessage());
    }

    @Test
    void shouldRejectNullOrientation() {
        assertThrows(NullPointerException.class, () -> new PageRule(
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(30),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(2),
                null
        ));
    }
}