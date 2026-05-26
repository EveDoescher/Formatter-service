package com.abntbuilder.formatter.output.docx.docx4j;

import com.abntbuilder.formatter.output.docx.api.DocxDocument;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.profile.model.PageOrientation;
import com.abntbuilder.formatter.profile.model.PageRule;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.StyleType;
import com.abntbuilder.formatter.profile.model.TextAlignment;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;

class Docx4jWriterTest {

    @Test
    void shouldWriteMinimalDocxDocument() throws IOException {
        DocxDocument document = new DocxDocument(
                validPageRule(),
                List.of(new DocxParagraph("Hello formatter", validStyleRule()))
        );

        byte[] bytes = new Docx4jWriter().write(document);

        assertTrue(bytes.length > 0);
        assertTrue(zipContains(bytes, "[Content_Types].xml"));
        assertTrue(zipContains(bytes, "word/document.xml"));

        String documentXml = readZipEntry(bytes, "word/document.xml");

        assertTrue(documentXml.contains("Hello formatter"));
        assertTrue(documentXml.contains("Test Font"));
    }

    @Test
    void shouldApplyUppercaseStyleWhenWritingParagraph() throws IOException {
        StyleRule uppercaseStyle = new StyleRule(
                "cover.title",
                StyleType.PARAGRAPH,
                "Test Font",
                BigDecimal.valueOf(12),
                TextAlignment.CENTER,
                BigDecimal.valueOf(1.5),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                true,
                false,
                true
        );

        DocxDocument document = new DocxDocument(
                validPageRule(),
                List.of(new DocxParagraph("Title example", uppercaseStyle))
        );

        byte[] bytes = new Docx4jWriter().write(document);

        String documentXml = readZipEntry(bytes, "word/document.xml");

        assertTrue(documentXml.contains("TITLE EXAMPLE"));
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

    private static PageRule validPageRule() {
        return new PageRule(
                BigDecimal.valueOf(21),
                BigDecimal.valueOf(29.7),
                BigDecimal.valueOf(3),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(3),
                PageOrientation.PORTRAIT
        );
    }

    private static StyleRule validStyleRule() {
        return new StyleRule(
                "body",
                StyleType.PARAGRAPH,
                "Test Font",
                BigDecimal.valueOf(12),
                TextAlignment.JUSTIFIED,
                BigDecimal.valueOf(1.5),
                BigDecimal.valueOf(1.25),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                false,
                false,
                false
        );
    }
}