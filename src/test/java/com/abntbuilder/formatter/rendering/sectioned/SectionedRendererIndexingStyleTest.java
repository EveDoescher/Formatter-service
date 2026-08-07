package com.abntbuilder.formatter.rendering.sectioned;

import com.abntbuilder.formatter.engine.model.profile.component.sectioned.SectionedComponentRule.IndexingStyle;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SectionedRendererIndexingStyleTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @ParameterizedTest
    @CsvSource({
            "ALPHABETIC,A",
            "ALPHABETIC_LOWER,a",
            "NUMERIC,1",
            "ROMAN_UPPER,I",
            "ROMAN_LOWER,i"
    })
    void acceptsAndRendersEverySupportedIndexingStyle(IndexingStyle style, String expectedMarker) throws Exception {
        assertEquals(style, objectMapper.readValue("\"" + style.name() + "\"", IndexingStyle.class));
        assertEquals(expectedMarker, SectionedRenderer.resolveMarker(style, 0));
    }
}
