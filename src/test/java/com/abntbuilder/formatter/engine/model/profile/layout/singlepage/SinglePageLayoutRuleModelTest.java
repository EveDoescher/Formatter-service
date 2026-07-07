package com.abntbuilder.formatter.engine.model.profile.layout.singlepage;

import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;
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
                        InvalidProfileStructureException.class,
                        () -> new LayoutGapRule(" ", "b", BigDecimal.ONE)
                ).getMessage()
        );

        assertEquals(
                "weight must be greater than zero.",
                assertThrows(
                        InvalidProfileStructureException.class,
                        () -> new LayoutGapRule("a", "b", BigDecimal.ZERO)
                ).getMessage()
        );
    }

    @Test
    void shouldRejectInvalidItemRule() {
        assertEquals(
                "id must not be blank.",
                assertThrows(
                        InvalidProfileStructureException.class,
                        () -> new SinglePageItemRule(" ", true, Optional.empty())
                ).getMessage()
        );

        assertEquals(
                "maxVisualLinesPerValue must be greater than zero.",
                assertThrows(
                        InvalidProfileStructureException.class,
                        () -> new SinglePageItemRule("city", true, Optional.of(0))
                ).getMessage()
        );

        assertEquals(
                "blankLinesAfter must not be negative.",
                assertThrows(
                        InvalidProfileStructureException.class,
                        () -> new SinglePageItemRule(
                                "nature",
                                true,
                                Optional.empty(),
                                HorizontalPlacementRule.fullContentWidth(),
                                -1
                        )
                ).getMessage()
        );
    }

    @Test
    void shouldUseFullContentWidthAsDefaultHorizontalPlacement() {
        SinglePageItemRule item = new SinglePageItemRule("nature", true, Optional.empty());

        assertEquals(HorizontalPlacementStrategy.FULL_CONTENT_WIDTH, item.horizontalPlacement().strategy());
    }

    @Test
    void shouldRejectNullHorizontalPlacement() {
        assertThrows(
                NullPointerException.class,
                () -> new SinglePageItemRule("nature", true, Optional.empty(), null)
        );
    }

    @Test
    void shouldRejectInvalidGroupRule() {
        SinglePageItemRule item = new SinglePageItemRule("title", true, Optional.empty());

        assertEquals(
                "items must not be empty.",
                assertThrows(
                        InvalidProfileStructureException.class,
                        () -> new SinglePageGroupRule("cover.title", true, List.of())
                ).getMessage()
        );

        assertEquals(
                "Duplicate single-page item id: title",
                assertThrows(
                        InvalidProfileStructureException.class,
                        () -> new SinglePageGroupRule("cover.title", true, List.of(item, item))
                ).getMessage()
        );
    }
}
