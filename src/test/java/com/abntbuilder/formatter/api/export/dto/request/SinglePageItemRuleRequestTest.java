package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.layout.singlepage.HorizontalPlacementStrategy;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageItemRule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SinglePageItemRuleRequestTest {

    @Test
    void shouldDefaultHorizontalPlacementToFullContentWidth() {
        SinglePageItemRuleRequest request = new SinglePageItemRuleRequest(
                "title",
                true,
                null,
                null
        );

        SinglePageItemRule rule = request.toDomain();

        assertEquals("title", rule.id());
        assertTrue(rule.required());
        assertTrue(rule.maxVisualLinesPerValue().isEmpty());
        assertEquals(HorizontalPlacementStrategy.FULL_CONTENT_WIDTH, rule.horizontalPlacement().strategy());
    }

    @Test
    void shouldConvertExplicitHorizontalPlacement() {
        SinglePageItemRuleRequest request = new SinglePageItemRuleRequest(
                "nature",
                true,
                3,
                HorizontalPlacementStrategy.FROM_PAGE_CENTER_TO_RIGHT_MARGIN
        );

        SinglePageItemRule rule = request.toDomain();

        assertEquals("nature", rule.id());
        assertTrue(rule.required());
        assertEquals(3, rule.maxVisualLinesPerValue().orElseThrow());
        assertEquals(
                HorizontalPlacementStrategy.FROM_PAGE_CENTER_TO_RIGHT_MARGIN,
                rule.horizontalPlacement().strategy()
        );
    }
}
