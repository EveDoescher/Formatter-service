package com.abntbuilder.formatter.document.component.flowtextual;

import com.abntbuilder.formatter.document.component.ComponentType;
import com.abntbuilder.formatter.document.component.singlepage.TextValue;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FlowTextualContentTest {

    @Test
    void shouldCreateWithValidData() {
        FlowTextualContent content = new FlowTextualContent(
                "dedication", Map.of("text", new TextValue("To my family.")));

        assertEquals("dedication", content.componentId());
        assertEquals(ComponentType.FLOW_TEXTUAL, content.type());
    }

    @Test
    void shouldAllowEmptySlots() {
        FlowTextualContent content = new FlowTextualContent("listOfAbbreviations", Map.of());

        assertEquals("listOfAbbreviations", content.componentId());
        assertTrue(content.slots().isEmpty());
    }

    @Test
    void shouldRejectBlankComponentId() {
        assertThrows(IllegalArgumentException.class, () ->
                new FlowTextualContent("  ", Map.of()));
    }

    @Test
    void shouldRejectNullComponentId() {
        assertThrows(IllegalArgumentException.class, () ->
                new FlowTextualContent(null, Map.of()));
    }

    @Test
    void shouldRejectNullSlots() {
        assertThrows(NullPointerException.class, () ->
                new FlowTextualContent("dedication", null));
    }

    @Test
    void shouldReturnImmutableSlots() {
        FlowTextualContent content = new FlowTextualContent(
                "dedication", Map.of("text", new TextValue("abc")));

        assertThrows(UnsupportedOperationException.class, () ->
                content.slots().put("extra", new TextValue("x")));
    }
}
