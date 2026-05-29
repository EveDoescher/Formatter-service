package com.abntbuilder.formatter.rendering.component.cover;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CoverSampleValidationTest {

    private static final Path COVER_SAMPLES_DIR = Path.of("docs", "samples", "cover");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldGenerateAllSuccessfulCoverSamplesFromOfficialJsonFiles() throws Exception {
        List<String> sampleNames = List.of(
                "cover-short.json",
                "cover-no-subtitle.json",
                "cover-long-title.json",
                "cover-many-authors.json",
                "cover-long-title-many-authors.json",
                "cover-limit.json"
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
    void shouldFailOverflowCoverSampleFromOfficialJsonFileBeforeGeneratingDocx() throws Exception {
        mockMvc.perform(post("/api/v1/exports/docx")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(readSample("cover-overflow.json")))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(
                        result.getResponse()
                                .getContentAsString()
                                .contains("Single-page layout overflow."),
                        "overflow sample should fail with a single-page overflow message."
                ));
    }

    @Test
    void shouldFailInvalidBottomWrapCoverSampleFromOfficialJsonFileBeforeGeneratingDocx() throws Exception {
        mockMvc.perform(post("/api/v1/exports/docx")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(readSample("cover-bottom-wrap-invalid.json")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "cover city and year must each fit in exactly one visual line."
                ));
    }

    private static String readSample(String sampleName) throws Exception {
        Path samplePath = COVER_SAMPLES_DIR.resolve(sampleName);

        assertTrue(Files.isRegularFile(samplePath), "Missing cover sample: " + sampleName);

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
