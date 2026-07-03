package com.abntbuilder.formatter.profile.model.component.singlepage;

import com.abntbuilder.formatter.profile.model.layout.singlepage.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SinglePageComponentRuleTest {

    @Test
    void shouldCreateValidRule() {
        SinglePageComponentRule rule = validRule();

        assertEquals("cover", rule.componentId());
        assertEquals(2, rule.slots().size());
        assertInstanceOf(TextSlotRule.class, rule.slots().get("title"));
        assertInstanceOf(TextListSlotRule.class, rule.slots().get("authors"));
        assertEquals("cover.title", rule.styleMapping().get("title"));
    }

    @Test
    void shouldRejectBlankComponentId() {
        assertThrows(IllegalArgumentException.class,
                () -> new SinglePageComponentRule("", Map.of(), Map.of(), minimalLayoutRule()));
        assertThrows(IllegalArgumentException.class,
                () -> new SinglePageComponentRule(null, Map.of(), Map.of(), minimalLayoutRule()));
    }

    @Test
    void shouldRejectNullSlots() {
        assertThrows(NullPointerException.class,
                () -> new SinglePageComponentRule("cover", null, Map.of(), minimalLayoutRule()));
    }

    @Test
    void shouldRejectNullStyleMapping() {
        assertThrows(NullPointerException.class,
                () -> new SinglePageComponentRule("cover", Map.of(), null, minimalLayoutRule()));
    }

    @Test
    void shouldRejectNullLayoutRule() {
        assertThrows(NullPointerException.class,
                () -> new SinglePageComponentRule("cover", Map.of(), Map.of(), null));
    }

    @Test
    void slotsShouldBeImmutable() {
        SinglePageComponentRule rule = validRule();
        assertThrows(UnsupportedOperationException.class,
                () -> rule.slots().put("city", new TextSlotRule(true)));
    }

    @Test
    void styleMappingShouldBeImmutable() {
        SinglePageComponentRule rule = validRule();
        assertThrows(UnsupportedOperationException.class,
                () -> rule.styleMapping().put("city", "cover.city"));
    }

    @Test
    void composedTextSlotRuleRejectsBlankTemplate() {
        assertThrows(IllegalArgumentException.class,
                () -> new ComposedTextSlotRule(true, "", List.of("workType")));
    }

    @Test
    void composedTextSlotRuleRejectsEmptyFieldNames() {
        assertThrows(IllegalArgumentException.class,
                () -> new ComposedTextSlotRule(true, "{workType}", List.of()));
    }

    @Test
    void signatureBlockListSlotRuleRequiresSignatureTextWhenEnabled() {
        assertThrows(IllegalArgumentException.class,
                () -> new SignatureBlockListSlotRule(true, true, "", List.of("{name}"), List.of("name")));
    }

    @Test
    void signatureBlockListSlotRuleAllowsNullSignatureTextWhenDisabled() {
        assertDoesNotThrow(
                () -> new SignatureBlockListSlotRule(false, false, null, List.of("{name}"), List.of("name")));
    }

    private static SinglePageComponentRule validRule() {
        return new SinglePageComponentRule(
                "cover",
                Map.of(
                        "title", new TextSlotRule(true),
                        "authors", new TextListSlotRule(true)
                ),
                Map.of(
                        "title", "cover.title",
                        "authors", "cover.author"
                ),
                minimalLayoutRule()
        );
    }

    private static SinglePageLayoutRule minimalLayoutRule() {
        SinglePageItemRule item = new SinglePageItemRule("title", true, (Integer) null);
        SinglePageGroupRule group = new SinglePageGroupRule("top", true, List.of(item));
        return new SinglePageLayoutRule(List.of(group), List.of(), SinglePageLayoutPolicy.defaultSinglePagePolicy());
    }
}
