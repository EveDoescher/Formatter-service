package com.abntbuilder.formatter.profile.model.component.cover;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CoverComponentRuleTest {

    @Test
    void shouldCreateValidCoverComponentRule() {
        CoverComponentRule rule = new CoverComponentRule(
                "cover",
                validStyleMapping(),
                validLayoutRule()
        );

        assertEquals("cover", rule.componentId());
        assertEquals("cover.top", rule.styleMapping().topLinesStyleId());
        assertEquals("cover.author", rule.styleMapping().authorLinesStyleId());
        assertEquals("cover.title", rule.styleMapping().titleStyleId());
        assertEquals("cover.subtitle", rule.styleMapping().subtitleStyleId());
        assertEquals("cover.bottom", rule.styleMapping().bottomLinesStyleId());

        assertEquals(0, BigDecimal.valueOf(45).compareTo(rule.layoutRule().topToAuthorWeight()));
        assertEquals(0, BigDecimal.valueOf(15).compareTo(rule.layoutRule().authorToTitleWeight()));
        assertEquals(0, BigDecimal.valueOf(40).compareTo(rule.layoutRule().titleToBottomWeight()));
        assertEquals(0, rule.layoutRule().safetyBlankLines());
    }

    @Test
    void shouldRejectBlankComponentId() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new CoverComponentRule(
                " ",
                validStyleMapping(),
                validLayoutRule()
        ));

        assertEquals("componentId must not be blank.", exception.getMessage());
    }

    @Test
    void shouldRejectNullStyleMapping() {
        assertThrows(NullPointerException.class, () -> new CoverComponentRule(
                "cover",
                null,
                validLayoutRule()
        ));
    }

    @Test
    void shouldRejectNullLayoutRule() {
        assertThrows(NullPointerException.class, () -> new CoverComponentRule(
                "cover",
                validStyleMapping(),
                null
        ));
    }

    @Test
    void shouldRejectBlankStyleIdInsideMapping() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new CoverStyleMapping(
                " ",
                "cover.author",
                "cover.title",
                "cover.subtitle",
                "cover.bottom"
        ));

        assertEquals("topLinesStyleId must not be blank.", exception.getMessage());
    }

    @Test
    void shouldRejectZeroTopToAuthorWeight() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new CoverLayoutRule(
                BigDecimal.ZERO,
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(40),
                0
        ));

        assertEquals("topToAuthorWeight must be greater than zero.", exception.getMessage());
    }

    @Test
    void shouldRejectZeroAuthorToTitleWeight() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new CoverLayoutRule(
                BigDecimal.valueOf(45),
                BigDecimal.ZERO,
                BigDecimal.valueOf(40),
                0
        ));

        assertEquals("authorToTitleWeight must be greater than zero.", exception.getMessage());
    }

    @Test
    void shouldRejectZeroTitleToBottomWeight() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new CoverLayoutRule(
                BigDecimal.valueOf(45),
                BigDecimal.valueOf(15),
                BigDecimal.ZERO,
                0
        ));

        assertEquals("titleToBottomWeight must be greater than zero.", exception.getMessage());
    }

    @Test
    void shouldRejectNegativeSafetyBlankLines() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new CoverLayoutRule(
                BigDecimal.valueOf(45),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(40),
                -1
        ));

        assertEquals("safetyBlankLines must not be negative.", exception.getMessage());
    }

    private static CoverStyleMapping validStyleMapping() {
        return new CoverStyleMapping(
                "cover.top",
                "cover.author",
                "cover.title",
                "cover.subtitle",
                "cover.bottom"
        );
    }

    private static CoverLayoutRule validLayoutRule() {
        return new CoverLayoutRule(
                BigDecimal.valueOf(45),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(40),
                0
        );
    }
}