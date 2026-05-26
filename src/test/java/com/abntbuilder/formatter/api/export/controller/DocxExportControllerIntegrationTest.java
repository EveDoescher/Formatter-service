package com.abntbuilder.formatter.api.export.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DocxExportControllerIntegrationTest {

    private static final String DOCX_MEDIA_TYPE =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldExportDocxFromJsonRequest() throws Exception {
        byte[] responseBytes = mockMvc.perform(post("/api/v1/exports/docx")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, DOCX_MEDIA_TYPE))
                .andExpect(header().string(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"teste-formatter.docx\""
                ))
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        assertTrue(responseBytes.length > 0);
        assertTrue(zipContains(responseBytes, "[Content_Types].xml"));
        assertTrue(zipContains(responseBytes, "word/document.xml"));

        String documentXml = readZipEntry(responseBytes, "word/document.xml");

        assertTrue(documentXml.contains("DOCUMENTO DE TESTE DO FORMATTER SERVICE"));
        assertTrue(documentXml.contains("Este parágrafo foi gerado a partir de dados enviados na requisição"));
        assertTrue(documentXml.contains("Times New Roman"));
    }

    @Test
    void shouldExportDocxWithCoverFromJsonRequest() throws Exception {
        byte[] responseBytes = mockMvc.perform(post("/api/v1/exports/docx")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCoverRequestJson()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, DOCX_MEDIA_TYPE))
                .andExpect(header().string(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"capa-teste.docx\""
                ))
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        assertTrue(responseBytes.length > 0);
        assertTrue(zipContains(responseBytes, "[Content_Types].xml"));
        assertTrue(zipContains(responseBytes, "word/document.xml"));

        String documentXml = readZipEntry(responseBytes, "word/document.xml");

        assertTrue(documentXml.contains("UNIVERSIDADE PAULISTA"));
        assertTrue(documentXml.contains("NOME COMPLETO DO ALUNO"));
        assertTrue(documentXml.contains("TÍTULO DO TRABALHO"));
        assertTrue(documentXml.contains("Limeira"));
        assertTrue(documentXml.contains("2026"));
    }

    @Test
    void shouldGenerateThenDownloadDocx() throws Exception {
        String generateResponseBody = mockMvc.perform(post("/api/v1/exports/docx/generated")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCoverRequestJson()))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.exportId").isNotEmpty())
                .andExpect(jsonPath("$.fileName").value("capa-teste.docx"))
                .andExpect(jsonPath("$.downloadUrl").isNotEmpty())
                .andExpect(jsonPath("$.sizeBytes").isNumber())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String downloadUrl = extractJsonStringValue(generateResponseBody, "downloadUrl");

        byte[] downloadedBytes = mockMvc.perform(get(downloadUrl))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, DOCX_MEDIA_TYPE))
                .andExpect(header().string(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"capa-teste.docx\""
                ))
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        assertTrue(downloadedBytes.length > 0);
        assertTrue(zipContains(downloadedBytes, "[Content_Types].xml"));
        assertTrue(zipContains(downloadedBytes, "word/document.xml"));

        String documentXml = readZipEntry(downloadedBytes, "word/document.xml");

        assertTrue(documentXml.contains("UNIVERSIDADE PAULISTA"));
        assertTrue(documentXml.contains("TÍTULO DO TRABALHO"));
        assertTrue(documentXml.contains("Limeira"));
        assertTrue(documentXml.contains("2026"));
    }

    @Test
    void shouldReturnNotFoundWhenGeneratedDocxDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/exports/docx/generated/not-found/download"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Generated DOCX export not found for id: not-found"));
    }

    @Test
    void shouldReturnBadRequestWhenRequiredFieldIsMissing() throws Exception {
        String invalidJson = """
                {
                  "profile": null,
                  "paragraphs": []
                }
                """;

        mockMvc.perform(post("/api/v1/exports/docx")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenParagraphReferencesMissingStyle() throws Exception {
        String invalidJson = """
                {
                  "fileName": "teste-formatter.docx",
                  "profile": {
                    "id": "test-profile",
                    "displayName": "Test Profile",
                    "pageRule": {
                      "widthCm": 21,
                      "heightCm": 29.7,
                      "marginTopCm": 3,
                      "marginRightCm": 2,
                      "marginBottomCm": 2,
                      "marginLeftCm": 3,
                      "orientation": "PORTRAIT"
                    },
                    "styleRules": [
                      {
                        "id": "body",
                        "type": "PARAGRAPH",
                        "fontFamily": "Times New Roman",
                        "fontSizePt": 12,
                        "alignment": "JUSTIFIED",
                        "lineSpacing": 1.5,
                        "firstLineIndentCm": 1.25,
                        "leftIndentCm": 0,
                        "rightIndentCm": 0,
                        "spacingBeforePt": 0,
                        "spacingAfterPt": 0,
                        "bold": false,
                        "italic": false,
                        "uppercase": false
                      }
                    ]
                  },
                  "paragraphs": [
                    {
                      "text": "Texto com estilo inexistente.",
                      "styleId": "missing.style"
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/exports/docx")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Missing style rule for id: missing.style"));
    }

    @Test
    void shouldReturnBadRequestWhenRequestBodyHasInvalidEnum() throws Exception {
        String invalidJson = """
                {
                  "fileName": "teste-formatter.docx",
                  "profile": {
                    "id": "test-profile",
                    "displayName": "Test Profile",
                    "pageRule": {
                      "widthCm": 21,
                      "heightCm": 29.7,
                      "marginTopCm": 3,
                      "marginRightCm": 2,
                      "marginBottomCm": 2,
                      "marginLeftCm": 3,
                      "orientation": "INVALID"
                    },
                    "styleRules": [
                      {
                        "id": "body",
                        "type": "PARAGRAPH",
                        "fontFamily": "Times New Roman",
                        "fontSizePt": 12,
                        "alignment": "JUSTIFIED",
                        "lineSpacing": 1.5,
                        "firstLineIndentCm": 1.25,
                        "leftIndentCm": 0,
                        "rightIndentCm": 0,
                        "spacingBeforePt": 0,
                        "spacingAfterPt": 0,
                        "bold": false,
                        "italic": false,
                        "uppercase": false
                      }
                    ]
                  },
                  "paragraphs": [
                    {
                      "text": "Texto de teste.",
                      "styleId": "body"
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/exports/docx")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid request body."));
    }

    private static String validRequestJson() {
        return """
                {
                  "fileName": "teste-formatter.docx",
                  "profile": {
                    "id": "test-profile",
                    "displayName": "Test Profile",
                    "pageRule": {
                      "widthCm": 21,
                      "heightCm": 29.7,
                      "marginTopCm": 3,
                      "marginRightCm": 2,
                      "marginBottomCm": 2,
                      "marginLeftCm": 3,
                      "orientation": "PORTRAIT"
                    },
                    "styleRules": [
                      {
                        "id": "body",
                        "type": "PARAGRAPH",
                        "fontFamily": "Times New Roman",
                        "fontSizePt": 12,
                        "alignment": "JUSTIFIED",
                        "lineSpacing": 1.5,
                        "firstLineIndentCm": 1.25,
                        "leftIndentCm": 0,
                        "rightIndentCm": 0,
                        "spacingBeforePt": 0,
                        "spacingAfterPt": 0,
                        "bold": false,
                        "italic": false,
                        "uppercase": false
                      },
                      {
                        "id": "title",
                        "type": "PARAGRAPH",
                        "fontFamily": "Times New Roman",
                        "fontSizePt": 12,
                        "alignment": "CENTER",
                        "lineSpacing": 1.5,
                        "firstLineIndentCm": 0,
                        "leftIndentCm": 0,
                        "rightIndentCm": 0,
                        "spacingBeforePt": 0,
                        "spacingAfterPt": 12,
                        "bold": true,
                        "italic": false,
                        "uppercase": true
                      }
                    ]
                  },
                  "paragraphs": [
                    {
                      "text": "Documento de teste do Formatter Service",
                      "styleId": "title"
                    },
                    {
                      "text": "Este parágrafo foi gerado a partir de dados enviados na requisição, usando um perfil informado pelo próprio JSON.",
                      "styleId": "body"
                    }
                  ]
                }
                """;
    }

    private static String validCoverRequestJson() {
        return """
                {
                  "fileName": "capa-teste.docx",
                  "profile": {
                    "id": "abnt-unip-profile",
                    "displayName": "ABNT UNIP Profile",
                    "pageRule": {
                      "widthCm": 21,
                      "heightCm": 29.7,
                      "marginTopCm": 3,
                      "marginRightCm": 2,
                      "marginBottomCm": 2,
                      "marginLeftCm": 3,
                      "orientation": "PORTRAIT"
                    },
                    "styleRules": [
                      {
                        "id": "cover.top",
                        "type": "PARAGRAPH",
                        "fontFamily": "Times New Roman",
                        "fontSizePt": 12,
                        "alignment": "CENTER",
                        "lineSpacing": 1.5,
                        "firstLineIndentCm": 0,
                        "leftIndentCm": 0,
                        "rightIndentCm": 0,
                        "spacingBeforePt": 0,
                        "spacingAfterPt": 0,
                        "bold": true,
                        "italic": false,
                        "uppercase": true
                      },
                      {
                        "id": "cover.author",
                        "type": "PARAGRAPH",
                        "fontFamily": "Times New Roman",
                        "fontSizePt": 12,
                        "alignment": "CENTER",
                        "lineSpacing": 1.5,
                        "firstLineIndentCm": 0,
                        "leftIndentCm": 0,
                        "rightIndentCm": 0,
                        "spacingBeforePt": 0,
                        "spacingAfterPt": 0,
                        "bold": false,
                        "italic": false,
                        "uppercase": true
                      },
                      {
                        "id": "cover.title",
                        "type": "PARAGRAPH",
                        "fontFamily": "Times New Roman",
                        "fontSizePt": 12,
                        "alignment": "CENTER",
                        "lineSpacing": 1.5,
                        "firstLineIndentCm": 0,
                        "leftIndentCm": 0,
                        "rightIndentCm": 0,
                        "spacingBeforePt": 0,
                        "spacingAfterPt": 0,
                        "bold": true,
                        "italic": false,
                        "uppercase": true
                      },
                      {
                        "id": "cover.subtitle",
                        "type": "PARAGRAPH",
                        "fontFamily": "Times New Roman",
                        "fontSizePt": 12,
                        "alignment": "CENTER",
                        "lineSpacing": 1.5,
                        "firstLineIndentCm": 0,
                        "leftIndentCm": 0,
                        "rightIndentCm": 0,
                        "spacingBeforePt": 0,
                        "spacingAfterPt": 0,
                        "bold": false,
                        "italic": false,
                        "uppercase": false
                      },
                      {
                        "id": "cover.bottom",
                        "type": "PARAGRAPH",
                        "fontFamily": "Times New Roman",
                        "fontSizePt": 12,
                        "alignment": "CENTER",
                        "lineSpacing": 1.5,
                        "firstLineIndentCm": 0,
                        "leftIndentCm": 0,
                        "rightIndentCm": 0,
                        "spacingBeforePt": 0,
                        "spacingAfterPt": 0,
                        "bold": false,
                        "italic": false,
                        "uppercase": false
                      }
                    ],
                    "componentRules": {
                      "cover": {
                        "componentId": "cover",
                        "styleMapping": {
                          "topLinesStyleId": "cover.top",
                          "authorLinesStyleId": "cover.author",
                          "titleStyleId": "cover.title",
                          "subtitleStyleId": "cover.subtitle",
                          "bottomLinesStyleId": "cover.bottom"
                        },
                        "layoutRule": {
                           "topToAuthorWeight": 30,
                           "authorToTitleWeight": 10,
                           "titleToBottomWeight": 60,
                           "bottomPaddingLineSlots": 1,
                           "maxCharactersPerLine": 52
                         }
                      }
                    }
                  },
                  "cover": {
                    "topLines": [
                      "UNIVERSIDADE PAULISTA"
                    ],
                    "authorLines": [
                      "NOME COMPLETO DO ALUNO"
                    ],
                    "title": "TÍTULO DO TRABALHO",
                    "subtitle": "Subtítulo do trabalho",
                    "bottomLines": [
                      "Limeira",
                      "2026"
                    ]
                  },
                  "paragraphs": []
                }
                """;
    }

    private static boolean zipContains(byte[] zipBytes, String entryName) throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;

            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entryName.equals(entry.getName())) {
                    return true;
                }
            }

            return false;
        }
    }

    private static String readZipEntry(byte[] zipBytes, String entryName) throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;

            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entryName.equals(entry.getName())) {
                    return new String(zipInputStream.readAllBytes(), StandardCharsets.UTF_8);
                }
            }

            throw new IllegalArgumentException("ZIP entry not found: " + entryName);
        }
    }

    private static String extractJsonStringValue(String json, String fieldName) {
        String fieldPattern = "\"" + fieldName + "\":\"";
        int fieldStart = json.indexOf(fieldPattern);

        if (fieldStart < 0) {
            throw new IllegalArgumentException("JSON field not found: " + fieldName);
        }

        int valueStart = fieldStart + fieldPattern.length();
        int valueEnd = json.indexOf("\"", valueStart);

        if (valueEnd < 0) {
            throw new IllegalArgumentException("Invalid JSON string value for field: " + fieldName);
        }

        return json.substring(valueStart, valueEnd);
    }
}