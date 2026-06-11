package com.abntbuilder.formatter.rendering.component.bodycontent;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BodyContentSampleValidationTest {

    private static final Path BODY_CONTENT_SAMPLES_DIR = Path.of("docs", "samples", "body-content");
    private static final Path COMPOSED_SAMPLES_DIR = Path.of("docs", "samples", "composed");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldGenerateAllSuccessfulBodyContentSamplesFromOfficialJsonFiles() throws Exception {
        List<String> sampleNames = List.of(
                "body-content-short.json",
                "body-content-citations.json",
                "body-content-figures.json",
                "body-content-title-only-section.json"
        );

        for (String sampleName : sampleNames) {
            byte[] responseBytes = mockMvc.perform(post("/api/v1/exports/docx")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(readSample(sampleName)))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsByteArray();

            assertTrue(responseBytes.length > 0, sampleName + " should generate a DOCX.");
            assertTrue(startsWithZipHeader(responseBytes), sampleName + " should generate ZIP/DOCX bytes.");
        }
    }

    @Test
    void shouldFailInvalidBodyContentSamplesFromOfficialJsonFilesBeforeGeneratingDocx() throws Exception {
        List<String> sampleNames = List.of(
                "body-content-section-hierarchy-invalid.json",
                "body-content-citation-direct-missing-page-invalid.json",
                "body-content-citation-manual-quotes-invalid.json",
                "body-content-selected-components-pagination-invalid.json"
        );

        for (String sampleName : sampleNames) {
            mockMvc.perform(post("/api/v1/exports/docx")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(readSample(sampleName)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void shouldGenerateComposedFullDocumentWithBodyContentSampleFromOfficialJsonFile() throws Exception {
        List<String> sampleNames = List.of(
                "full-document-with-body-content.json",
                "full-document-with-work-bindings.json"
        );

        for (String sampleName : sampleNames) {
            byte[] responseBytes = mockMvc.perform(post("/api/v1/exports/docx")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(readComposedSample(sampleName)))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsByteArray();

            assertTrue(responseBytes.length > 0, sampleName + " should generate a DOCX.");
            assertTrue(startsWithZipHeader(responseBytes), sampleName + " should generate ZIP/DOCX bytes.");
        }
    }

    private static String readSample(String sampleName) throws Exception {
        Path samplePath = BODY_CONTENT_SAMPLES_DIR.resolve(sampleName);

        assertTrue(Files.isRegularFile(samplePath), "Missing bodyContent sample: " + sampleName);

        return Files.readString(samplePath);
    }

    private static String readComposedSample(String sampleName) throws Exception {
        Path samplePath = COMPOSED_SAMPLES_DIR.resolve(sampleName);

        assertTrue(Files.isRegularFile(samplePath), "Missing composed sample: " + sampleName);

        return Files.readString(samplePath);
    }

    private static boolean startsWithZipHeader(byte[] bytes) {
        return bytes.length >= 4
                && bytes[0] == 'P'
                && bytes[1] == 'K'
                && bytes[2] == 3
                && bytes[3] == 4;
    }
}
