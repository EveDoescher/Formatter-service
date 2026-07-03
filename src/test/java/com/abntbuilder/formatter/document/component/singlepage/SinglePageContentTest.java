package com.abntbuilder.formatter.document.component.singlepage;

import com.abntbuilder.formatter.document.component.ComponentType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SinglePageContentTest {

    @Test
    void shouldCreateValidContent() {
        SinglePageContent content = new SinglePageContent(
                "cover",
                Map.of(
                        "title", new TextValue("Título do Trabalho"),
                        "authors", new TextListValue(java.util.List.of("Ana Souza")),
                        "city", new TextValue("Limeira"),
                        "year", new TextValue("2026")
                )
        );

        assertEquals("cover", content.componentId());
        assertEquals(ComponentType.SINGLE_PAGE, content.type());
        assertEquals(4, content.slots().size());
        assertInstanceOf(TextValue.class, content.slots().get("title"));
    }

    @Test
    void shouldRejectBlankComponentId() {
        assertThrows(IllegalArgumentException.class,
                () -> new SinglePageContent("", Map.of("title", new TextValue("x"))));
        assertThrows(IllegalArgumentException.class,
                () -> new SinglePageContent(null, Map.of("title", new TextValue("x"))));
    }

    @Test
    void shouldRejectNullSlots() {
        assertThrows(NullPointerException.class,
                () -> new SinglePageContent("cover", null));
    }

    @Test
    void slotsShouldBeImmutable() {
        SinglePageContent content = new SinglePageContent(
                "cover",
                Map.of("title", new TextValue("Título"))
        );
        assertThrows(UnsupportedOperationException.class,
                () -> content.slots().put("extra", new TextValue("x")));
    }

    @Test
    void shouldAllowEmptySlots() {
        SinglePageContent content = new SinglePageContent("cover", Map.of());
        assertTrue(content.slots().isEmpty());
    }
}
