package com.abntbuilder.formatter.rendering.orchestration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ComponentSampleValidationTest {

    private static final Path SAMPLES_DIR = Path.of("docs", "samples");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRenderAbstractSample() throws Exception {
        assertSampleOk("resumo/resumo-simple.json");
    }

    @Test
    void shouldRenderListOfSymbolsSample() throws Exception {
        assertSampleOk("list-of-symbols/list-of-symbols-simple.json");
    }

    @Test
    void shouldRenderListOfAbbreviationsWithBodyContentSample() throws Exception {
        assertSampleOk("list-of-abbreviations/list-of-abbreviations-with-body.json");
    }

    @Test
    void shouldRenderSummarySample() throws Exception {
        assertSampleOk("summary/summary-simple.json");
    }

    @Test
    void shouldRenderBodyContentWithAllIndexListsSample() throws Exception {
        assertSampleOk("composed/body-content-with-index-lists.json");
    }

    @Test
    void shouldRenderFullAbntCompleteDocumentSample() throws Exception {
        assertSampleOk("composed/full-document-abnt-complete.json");
    }

    private void assertSampleOk(String relativePath) throws Exception {
        Path samplePath = SAMPLES_DIR.resolve(relativePath);
        assertTrue(Files.isRegularFile(samplePath), "Missing sample: " + relativePath);

        byte[] responseBytes = mockMvc.perform(post("/api/v1/exports/docx")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(Files.readString(samplePath)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        assertTrue(responseBytes.length > 0, relativePath + " should generate a DOCX.");
        assertTrue(startsWithZipHeader(responseBytes), relativePath + " should generate ZIP/DOCX bytes.");
    }

    private static boolean startsWithZipHeader(byte[] bytes) {
        return bytes.length >= 4
                && bytes[0] == 'P'
                && bytes[1] == 'K'
                && bytes[2] == 3
                && bytes[3] == 4;
    }
}
