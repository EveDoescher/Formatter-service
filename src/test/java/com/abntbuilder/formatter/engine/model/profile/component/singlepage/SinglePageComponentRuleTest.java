package com.abntbuilder.formatter.engine.model.profile.component.singlepage;

import com.abntbuilder.formatter.engine.model.profile.layout.singlepage.*;
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
                () -> new SinglePageComponentRule("", true, null, Map.of(), Map.of(), minimalLayoutRule()));
        assertThrows(IllegalArgumentException.class,
                () -> new SinglePageComponentRule(null, true, null, Map.of(), Map.of(), minimalLayoutRule()));
    }

    @Test
    void shouldRejectNullSlots() {
        assertThrows(NullPointerException.class,
                () -> new SinglePageComponentRule("cover", true, null, null, Map.of(), minimalLayoutRule()));
    }

    @Test
    void shouldRejectNullStyleMapping() {
        assertThrows(NullPointerException.class,
                () -> new SinglePageComponentRule("cover", true, null, Map.of(), null, minimalLayoutRule()));
    }

    @Test
    void shouldRejectNullLayoutRule() {
        assertThrows(NullPointerException.class,
                () -> new SinglePageComponentRule("cover", true, null, Map.of(), Map.of(), null));
    }

    @Test
    void slotsShouldBeImmutable() {
        SinglePageComponentRule rule = validRule();
        assertThrows(UnsupportedOperationException.class,
                () -> rule.slots().put("city", new TextSlotRule(true, null, null)));
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
                () -> new ComposedTextSlotRule(true, null, null, "", List.of("workType")));
    }

    @Test
    void composedTextSlotRuleRejectsEmptyFieldNames() {
        assertThrows(IllegalArgumentException.class,
                () -> new ComposedTextSlotRule(true, null, null, "{workType}", List.of()));
    }

    @Test
    void signatureBlockListSlotRuleRequiresSignatureTextWhenEnabled() {
        assertThrows(IllegalArgumentException.class,
                () -> new SignatureBlockListSlotRule(true, null, null, true, "", List.of("{name}"), List.of("name")));
    }

    @Test
    void signatureBlockListSlotRuleAllowsNullSignatureTextWhenDisabled() {
        assertDoesNotThrow(
                () -> new SignatureBlockListSlotRule(false, null, null, false, null, List.of("{name}"), List.of("name")));
    }

    private static SinglePageComponentRule validRule() {
        return new SinglePageComponentRule(
                "cover",
                true,
                null,
                Map.of(
                        "title", new TextSlotRule(true, null, null),
                        "authors", new TextListSlotRule(true, null, null)
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
