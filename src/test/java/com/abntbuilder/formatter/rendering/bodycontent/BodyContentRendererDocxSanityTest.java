package com.abntbuilder.formatter.rendering.bodycontent;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BodyContentRendererDocxSanityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void inlineFormattingSampleShouldContainMultipleRuns() throws Exception {
        String json = Files.readString(
                Path.of("docs/samples/body-content/body-content-inline-formatting.json")
        );

        byte[] docxBytes = mockMvc.perform(post("/api/v1/exports/docx")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        Document wordDoc = extractXmlEntry(docxBytes, "word/document.xml");
        NodeList runs = wordDoc.getElementsByTagNameNS(
                "http://schemas.openxmlformats.org/wordprocessingml/2006/main", "r"
        );
        assertThat(runs.getLength()).isGreaterThan(1);
    }

    @Test
    void headingStylesShouldHaveBasedOnNormal() throws Exception {
        String json = Files.readString(
                Path.of("docs/samples/body-content/body-content-short.json")
        );

        byte[] docxBytes = mockMvc.perform(post("/api/v1/exports/docx")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        Document stylesDoc = extractXmlEntry(docxBytes, "word/styles.xml");
        NodeList basedOnNodes = stylesDoc.getElementsByTagNameNS(
                "http://schemas.openxmlformats.org/wordprocessingml/2006/main", "basedOn"
        );
        assertThat(basedOnNodes.getLength()).isGreaterThan(0);
    }

    private static Document extractXmlEntry(byte[] docxBytes, String entryName) throws Exception {
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(docxBytes))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entryName.equals(entry.getName())) {
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    dbf.setNamespaceAware(true);
                    return dbf.newDocumentBuilder().parse(zip);
                }
            }
        }
        throw new AssertionError("Entry not found in DOCX: " + entryName);
    }
}
