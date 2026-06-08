package com.abntbuilder.formatter.rendering.component.titlepage;

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
class TitlePageSampleValidationTest {

    private static final Path TITLE_PAGE_SAMPLES_DIR = Path.of("docs", "samples", "title-page");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldGenerateAllSuccessfulTitlePageSamplesFromOfficialJsonFiles() throws Exception {
        List<String> sampleNames = List.of(
                "title-page-short.json",
                "title-page-no-subtitle.json",
                "title-page-with-coadvisor.json",
                "title-page-long-title.json",
                "title-page-long-nature.json",
                "title-page-many-authors.json",
                "cover-and-title-page.json"
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
    void shouldFailOverflowTitlePageSampleFromOfficialJsonFileBeforeGeneratingDocx() throws Exception {
        mockMvc.perform(post("/api/v1/exports/docx")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(readSample("title-page-overflow.json")))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(
                        result.getResponse()
                                .getContentAsString()
                                .contains("Single-page layout overflow."),
                        "overflow sample should fail with a single-page overflow message."
                ));
    }

    @Test
    void shouldFailInvalidBottomWrapTitlePageSampleFromOfficialJsonFileBeforeGeneratingDocx() throws Exception {
        mockMvc.perform(post("/api/v1/exports/docx")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(readSample("title-page-bottom-wrap-invalid.json")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "titlePage item exceeds max visual lines: city must fit in 1 visual line(s)."
                ));
    }

    private static String readSample(String sampleName) throws Exception {
        Path samplePath = TITLE_PAGE_SAMPLES_DIR.resolve(sampleName);

        assertTrue(Files.isRegularFile(samplePath), "Missing titlePage sample: " + sampleName);

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
