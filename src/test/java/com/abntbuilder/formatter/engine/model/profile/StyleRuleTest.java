package com.abntbuilder.formatter.engine.model.profile;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class StyleRuleTest {

    @Test
    void shouldCreateValidStyleRule() {
        StyleRule rule = validStyleRule();

        assertEquals("body", rule.id());
        assertEquals(StyleType.PARAGRAPH, rule.type());
        assertEquals("Test Font", rule.fontFamily());
        assertEquals(TextAlignment.JUSTIFIED, rule.alignment());
        assertFalse(rule.bold());
        assertFalse(rule.italic());
        assertFalse(rule.uppercase());
    }

    @Test
    void shouldRejectBlankId() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new StyleRule(
                " ",
                StyleType.PARAGRAPH,
                "Test Font",
                BigDecimal.valueOf(12),
                TextAlignment.LEFT,
                BigDecimal.valueOf(1.5),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                false,
                false,
                false
        ));

        assertEquals("id must not be blank.", exception.getMessage());
    }

    @Test
    void shouldRejectNullType() {
        assertThrows(NullPointerException.class, () -> new StyleRule(
                "body",
                null,
                "Test Font",
                BigDecimal.valueOf(12),
                TextAlignment.LEFT,
                BigDecimal.valueOf(1.5),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                false,
                false,
                false
        ));
    }

    @Test
    void shouldRejectBlankFontFamily() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new StyleRule(
                "body",
                StyleType.PARAGRAPH,
                "",
                BigDecimal.valueOf(12),
                TextAlignment.LEFT,
                BigDecimal.valueOf(1.5),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                false,
                false,
                false
        ));

        assertEquals("fontFamily must not be blank.", exception.getMessage());
    }

    @Test
    void shouldRejectZeroFontSize() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new StyleRule(
                "body",
                StyleType.PARAGRAPH,
                "Test Font",
                BigDecimal.ZERO,
                TextAlignment.LEFT,
                BigDecimal.valueOf(1.5),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                false,
                false,
                false
        ));

        assertEquals("fontSizePt must be greater than zero.", exception.getMessage());
    }

    @Test
    void shouldRejectNullAlignment() {
        assertThrows(NullPointerException.class, () -> new StyleRule(
                "body",
                StyleType.PARAGRAPH,
                "Test Font",
                BigDecimal.valueOf(12),
                null,
                BigDecimal.valueOf(1.5),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                false,
                false,
                false
        ));
    }

    @Test
    void shouldRejectZeroLineSpacing() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new StyleRule(
                "body",
                StyleType.PARAGRAPH,
                "Test Font",
                BigDecimal.valueOf(12),
                TextAlignment.LEFT,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                false,
                false,
                false
        ));

        assertEquals("lineSpacing must be greater than zero.", exception.getMessage());
    }

    @Test
    void shouldAcceptNegativeFirstLineIndentForHangingIndent() {
        StyleRule rule = new StyleRule(
                "body",
                StyleType.PARAGRAPH,
                "Test Font",
                BigDecimal.valueOf(12),
                TextAlignment.LEFT,
                BigDecimal.valueOf(1.5),
                BigDecimal.valueOf(-1.25),
                BigDecimal.valueOf(1.25),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                false,
                false,
                false
        );
        assertEquals(BigDecimal.valueOf(-1.25), rule.firstLineIndentCm());
    }

    @Test
    void shouldRejectNegativeSpacingBefore() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new StyleRule(
                "body",
                StyleType.PARAGRAPH,
                "Test Font",
                BigDecimal.valueOf(12),
                TextAlignment.LEFT,
                BigDecimal.valueOf(1.5),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.valueOf(-1),
                BigDecimal.ZERO,
                false,
                false,
                false
        ));

        assertEquals("spacingBeforePt must not be negative.", exception.getMessage());
    }

    private static StyleRule validStyleRule() {
        return new StyleRule(
                "body",
                StyleType.PARAGRAPH,
                "Test Font",
                BigDecimal.valueOf(12),
                TextAlignment.JUSTIFIED,
                BigDecimal.valueOf(1.5),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                false,
                false,
                false
        );
    }
}