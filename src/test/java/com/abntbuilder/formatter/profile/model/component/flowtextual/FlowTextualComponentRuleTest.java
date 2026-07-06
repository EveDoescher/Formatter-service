package com.abntbuilder.formatter.profile.model.component.flowtextual;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlowTextualComponentRuleTest {

    @Test
    void shouldCreateWithValidData() {
        FlowTextualComponentRule rule = new FlowTextualComponentRule(
                "dedication",
                List.of(new FlowItem.PlainTextItem("dedication.text", "text"))
        );

        assertEquals("dedication", rule.componentId());
        assertEquals(1, rule.items().size());
    }

    @Test
    void shouldRejectBlankComponentId() {
        assertThrows(IllegalArgumentException.class, () ->
                new FlowTextualComponentRule("", List.of(new FlowItem.HeadingItem("s", "t"))));
    }

    @Test
    void shouldRejectEmptyItems() {
        assertThrows(IllegalArgumentException.class, () ->
                new FlowTextualComponentRule("dedication", List.of()));
    }

    @Test
    void shouldReturnImmutableItems() {
        FlowTextualComponentRule rule = new FlowTextualComponentRule(
                "dedication",
                List.of(new FlowItem.PlainTextItem("s", "text"))
        );

        assertThrows(UnsupportedOperationException.class, () ->
                rule.items().add(new FlowItem.HeadingItem("s", "h")));
    }

    @Test
    void shouldReturnEmptyContentBindings() {
        FlowTextualComponentRule rule = new FlowTextualComponentRule(
                "dedication",
                List.of(new FlowItem.PlainTextItem("s", "text"))
        );

        assertTrue(rule.contentBindings().isEmpty());
    }
}
