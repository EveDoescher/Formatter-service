package com.abntbuilder.formatter.profile.model.layout.singlepage;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SinglePageLayoutRuleModelTest {

    @Test
    void shouldRejectInvalidGapRule() {
        assertEquals(
                "fromGroupId must not be blank.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new LayoutGapRule(" ", "b", BigDecimal.ONE)
                ).getMessage()
        );

        assertEquals(
                "weight must be greater than zero.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new LayoutGapRule("a", "b", BigDecimal.ZERO)
                ).getMessage()
        );
    }

    @Test
    void shouldRejectInvalidItemRule() {
        assertEquals(
                "id must not be blank.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new SinglePageItemRule(" ", true, Optional.empty())
                ).getMessage()
        );

        assertEquals(
                "maxVisualLinesPerValue must be greater than zero.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new SinglePageItemRule("city", true, Optional.of(0))
                ).getMessage()
        );
    }

    @Test
    void shouldRejectInvalidGroupRule() {
        SinglePageItemRule item = new SinglePageItemRule("title", true, Optional.empty());

        assertEquals(
                "items must not be empty.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new SinglePageGroupRule("cover.title", true, List.of())
                ).getMessage()
        );

        assertEquals(
                "Duplicate single-page item id: title",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new SinglePageGroupRule("cover.title", true, List.of(item, item))
                ).getMessage()
        );
    }
}
