package com.abntbuilder.formatter.output.docx;

import com.abntbuilder.formatter.engine.model.output.DocxDocument;
import com.abntbuilder.formatter.engine.model.output.DocxParagraph;
import com.abntbuilder.formatter.engine.model.output.DocxRun;
import com.abntbuilder.formatter.engine.model.output.DocxSectionBreak;
import com.abntbuilder.formatter.engine.model.output.ParagraphLayoutOverride;
import com.abntbuilder.formatter.engine.model.profile.PageOrientation;
import com.abntbuilder.formatter.engine.model.profile.PageRule;
import com.abntbuilder.formatter.engine.model.profile.StyleRule;
import com.abntbuilder.formatter.engine.model.profile.StyleType;
import com.abntbuilder.formatter.engine.model.profile.TextAlignment;
import com.abntbuilder.formatter.shared.measurement.MeasurementConverter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;

class Docx4jWriterTest {

    @Test
    void shouldWriteMinimalDocxDocument() throws IOException {
        StyleRule style = validStyleRule();
        DocxDocument document = new DocxDocument(
                validPageRule(),
                List.of(new DocxParagraph(List.of(DocxRun.of("Hello formatter", style)), style))
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
                List.of(new DocxParagraph(List.of(DocxRun.of("Title example", uppercaseStyle)), uppercaseStyle))
        );

        byte[] bytes = new Docx4jWriter().write(document);

        String documentXml = readZipEntry(bytes, "word/document.xml");

        assertTrue(documentXml.contains("TITLE EXAMPLE"));
    }

    @Test
    void shouldApplyParagraphLayoutOverrideWhenWritingParagraph() throws IOException {
        BigDecimal leftIndentCm = BigDecimal.valueOf(8);
        StyleRule style = validStyleRule();
        DocxDocument document = new DocxDocument(
                validPageRule(),
                List.of(new DocxParagraph(
                        List.of(DocxRun.of("Nature block", style)),
                        style,
                        Optional.empty(),
                        Optional.empty(),
                        Optional.of(new ParagraphLayoutOverride(
                                Optional.of(leftIndentCm),
                                Optional.of(BigDecimal.ZERO),
                                Optional.of(TextAlignment.LEFT)
                        ))
                ))
        );

        byte[] bytes = new Docx4jWriter().write(document);

        String documentXml = readZipEntry(bytes, "word/document.xml");

        assertTrue(documentXml.contains("w:left=\""
                + MeasurementConverter.centimetersToTwips(leftIndentCm)
                + "\""));
        assertTrue(documentXml.contains("w:val=\"left\""));
    }

    @Test
    void shouldCustomizeAndApplyWordHeadingStyleForHeadingStyleRules() throws IOException {
        StyleRule headingStyle = new StyleRule(
                "bodyContent.heading1",
                StyleType.HEADING_1,
                "Test Font",
                BigDecimal.valueOf(12),
                TextAlignment.LEFT,
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
                List.of(new DocxParagraph(List.of(DocxRun.of("1 Introducao", headingStyle)), headingStyle))
        );

        byte[] bytes = new Docx4jWriter().write(document);

        String documentXml = readZipEntry(bytes, "word/document.xml");
        String stylesXml = readZipEntry(bytes, "word/styles.xml");

        assertTrue(documentXml.contains("w:pStyle"));
        assertTrue(documentXml.contains("w:val=\"Heading1\""));
        assertFalse(documentXml.contains("Test Font"));
        assertTrue(stylesXml.contains("w:styleId=\"Heading1\""));
        assertTrue(stylesXml.contains("w:outlineLvl"));
        assertTrue(stylesXml.contains("w:val=\"0\""));
        assertTrue(stylesXml.contains("Test Font"));
        assertTrue(stylesXml.contains("w:before=\"0\""));
        assertTrue(stylesXml.contains("w:after=\"0\""));
    }

    @Test
    void shouldWritePageNumberingHeaderWhenInitialPageNumberingIsPresent() throws IOException {
        StyleRule style = validStyleRule();
        DocxDocument document = new DocxDocument(
                validPageRule(),
                Optional.of(new com.abntbuilder.formatter.engine.model.output.DocxPageNumbering(
                        style,
                        com.abntbuilder.formatter.engine.model.profile.PageNumberingPlacement.HEADER_RIGHT,
                        true,
                        true,
                        BigDecimal.valueOf(2),
                        BigDecimal.valueOf(2)
                )),
                List.of(new DocxParagraph(List.of(DocxRun.of("Body text", style)), style))
        );

        byte[] bytes = new Docx4jWriter().write(document);

        String documentXml = readZipEntry(bytes, "word/document.xml");
        String headerXml = readZipEntryStartingWith(bytes, "word/header");

        assertTrue(documentXml.contains("w:headerReference"));
        assertTrue(documentXml.contains("w:pgNumType"));
        assertTrue(documentXml.contains("w:header=\"1134\""));
        assertTrue(headerXml.contains("PAGE"));
        assertTrue(headerXml.contains("w:jc"));
        assertTrue(headerXml.contains("w:val=\"right\""));
    }

    @Test
    void shouldApplyPageNumberingOnlyToSectionAfterSectionBreak() throws IOException {
        StyleRule style = validStyleRule();
        DocxDocument document = new DocxDocument(
                validPageRule(),
                List.of(
                        new DocxParagraph(List.of(DocxRun.of("Pre textual content", style)), style),
                        new DocxSectionBreak(new com.abntbuilder.formatter.engine.model.output.DocxPageNumbering(
                                style,
                                com.abntbuilder.formatter.engine.model.profile.PageNumberingPlacement.HEADER_RIGHT,
                                true,
                                false,
                                BigDecimal.valueOf(2),
                                BigDecimal.valueOf(2)
                        )),
                        new DocxParagraph(List.of(DocxRun.of("Hidden counted content", style)), style),
                        new DocxSectionBreak(new com.abntbuilder.formatter.engine.model.output.DocxPageNumbering(
                                style,
                                com.abntbuilder.formatter.engine.model.profile.PageNumberingPlacement.HEADER_RIGHT,
                                false,
                                true,
                                BigDecimal.valueOf(2),
                                BigDecimal.valueOf(2)
                        )),
                        new DocxParagraph(List.of(DocxRun.of("Body content", style)), style)
                )
        );

        byte[] bytes = new Docx4jWriter().write(document);

        String documentXml = readZipEntry(bytes, "word/document.xml");
        String headerXml = readZipEntryStartingWith(bytes, "word/header");
        int bodyContentIndex = documentXml.indexOf("Body content");
        int headerReferenceIndex = documentXml.indexOf("w:headerReference");

        assertTrue(bodyContentIndex > 0);
        assertTrue(headerReferenceIndex > bodyContentIndex);
        assertTrue(documentXml.contains("w:header=\"1134\""));
        assertTrue(headerXml.contains("PAGE"));
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

    private static String readZipEntryStartingWith(byte[] zipBytes, String entryNamePrefix) throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;

            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.getName().startsWith(entryNamePrefix)) {
                    return new String(zipInputStream.readAllBytes(), StandardCharsets.UTF_8);
                }
            }

            throw new IllegalArgumentException("ZIP entry not found with prefix: " + entryNamePrefix);
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
