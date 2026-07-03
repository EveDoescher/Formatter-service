package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.singlepage.ComposedTextValue;
import com.abntbuilder.formatter.document.component.singlepage.SinglePageContent;
import com.abntbuilder.formatter.document.component.singlepage.TextListValue;
import com.abntbuilder.formatter.document.component.singlepage.TextValue;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExportDocxRequestTest {

    @Test
    void shouldConvertTextSlotToTextValue() {
        SinglePageContentRequest request = new SinglePageContentRequest();
        request.setSlot("title", "Título do Trabalho");

        SinglePageContent content = request.toDomain("cover");

        assertEquals("cover", content.componentId());
        assertInstanceOf(TextValue.class, content.slots().get("title"));
        assertEquals("Título do Trabalho", ((TextValue) content.slots().get("title")).text());
    }

    @Test
    void shouldConvertStringListSlotToTextListValue() {
        SinglePageContentRequest request = new SinglePageContentRequest();
        request.setSlot("authors", List.of("Ana Souza", "Carlos Lima"));

        SinglePageContent content = request.toDomain("cover");

        assertInstanceOf(TextListValue.class, content.slots().get("authors"));
        assertEquals(List.of("Ana Souza", "Carlos Lima"),
                ((TextListValue) content.slots().get("authors")).items());
    }

    @Test
    void shouldConvertMapSlotToComposedTextValue() {
        SinglePageContentRequest request = new SinglePageContentRequest();
        request.setSlot("nature", Map.of("workType", "TCC", "courseName", "ADS"));

        SinglePageContent content = request.toDomain("titlePage");

        assertInstanceOf(ComposedTextValue.class, content.slots().get("nature"));
        assertEquals("TCC", ((ComposedTextValue) content.slots().get("nature")).fields().get("workType"));
    }

    @Test
    void shouldSkipNullSlots() {
        SinglePageContentRequest request = new SinglePageContentRequest();
        request.setSlot("title", "Título");
        request.setSlot("subtitle", null);

        SinglePageContent content = request.toDomain("cover");

        assertTrue(content.slots().containsKey("title"));
        assertFalse(content.slots().containsKey("subtitle"));
    }

    @Test
    void shouldThrowForEmptyList() {
        SinglePageContentRequest request = new SinglePageContentRequest();
        request.setSlot("authors", List.of());

        assertThrows(IllegalArgumentException.class, () -> request.toDomain("cover"));
    }

    @Test
    void shouldThrowForUnsupportedType() {
        SinglePageContentRequest request = new SinglePageContentRequest();
        request.setSlot("weird", 42);

        assertThrows(IllegalArgumentException.class, () -> request.toDomain("cover"));
    }
}
