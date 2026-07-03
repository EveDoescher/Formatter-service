package com.abntbuilder.formatter.document.component.singlepage;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ContentValueTest {

    @Test
    void textValueRejectsBlank() {
        assertThrows(IllegalArgumentException.class, () -> new TextValue(""));
        assertThrows(IllegalArgumentException.class, () -> new TextValue("   "));
        assertThrows(IllegalArgumentException.class, () -> new TextValue(null));
    }

    @Test
    void textValueAcceptsNonBlank() {
        TextValue v = new TextValue("Título do Trabalho");
        assertEquals("Título do Trabalho", v.text());
    }

    @Test
    void textListValueRejectsEmptyList() {
        assertThrows(IllegalArgumentException.class, () -> new TextListValue(List.of()));
    }

    @Test
    void textListValueRejectsBlankItem() {
        assertThrows(IllegalArgumentException.class, () -> new TextListValue(List.of("Ana Souza", "")));
    }

    @Test
    void textListValueIsImmutable() {
        TextListValue v = new TextListValue(List.of("Ana Souza", "Carlos Lima"));
        assertEquals(2, v.items().size());
        assertThrows(UnsupportedOperationException.class, () -> v.items().add("extra"));
    }

    @Test
    void composedTextValueRejectsEmptyMap() {
        assertThrows(IllegalArgumentException.class, () -> new ComposedTextValue(Map.of()));
    }

    @Test
    void composedTextValueRejectsBlankValue() {
        assertThrows(IllegalArgumentException.class, () -> new ComposedTextValue(Map.of("key", "")));
    }

    @Test
    void composedTextValueIsImmutable() {
        ComposedTextValue v = new ComposedTextValue(Map.of("workType", "TCC", "courseName", "ADS"));
        assertEquals("TCC", v.fields().get("workType"));
        assertThrows(UnsupportedOperationException.class, () -> v.fields().put("x", "y"));
    }

    @Test
    void signatureBlockListValueRejectsEmptyList() {
        assertThrows(IllegalArgumentException.class, () -> new SignatureBlockListValue(List.of()));
    }

    @Test
    void signatureBlockListValueRejectsEmptyEntry() {
        assertThrows(IllegalArgumentException.class,
                () -> new SignatureBlockListValue(List.of(Map.of())));
    }

    @Test
    void signatureBlockListValueRejectsBlankFieldValue() {
        assertThrows(IllegalArgumentException.class,
                () -> new SignatureBlockListValue(List.of(Map.of("name", ""))));
    }

    @Test
    void signatureBlockListValueIsImmutable() {
        SignatureBlockListValue v = new SignatureBlockListValue(
                List.of(Map.of("name", "Prof. Dr. Carlos Lima", "role", "Orientador")));
        assertEquals(1, v.entries().size());
        assertThrows(UnsupportedOperationException.class, () -> v.entries().add(Map.of("name", "extra")));
    }
}
