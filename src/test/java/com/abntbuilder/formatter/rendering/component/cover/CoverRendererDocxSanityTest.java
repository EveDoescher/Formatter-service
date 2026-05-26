package com.abntbuilder.formatter.rendering.component.cover;

import com.abntbuilder.formatter.document.component.cover.CoverComponent;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxDocument;
import com.abntbuilder.formatter.output.docx.docx4j.Docx4jWriter;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.PageOrientation;
import com.abntbuilder.formatter.profile.model.PageRule;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.StyleType;
import com.abntbuilder.formatter.profile.model.TextAlignment;
import com.abntbuilder.formatter.profile.model.component.cover.CoverComponentRule;
import com.abntbuilder.formatter.profile.model.component.cover.CoverLayoutRule;
import com.abntbuilder.formatter.profile.model.component.cover.CoverStyleMapping;
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

class CoverRendererDocxSanityTest {

    @Test
    void shouldGenerateCoverDocxXmlWithExpectedContentAndExactLineSpacing() throws IOException {
        DocumentProfile profile = validProfile();

        List<DocxBlock> blocks = new CoverRenderer().render(validCover(), profile);

        byte[] bytes = new Docx4jWriter().write(new DocxDocument(
                profile.pageRule(),
                blocks
        ));

        assertTrue(bytes.length > 0);
        assertTrue(zipContains(bytes, "[Content_Types].xml"));
        assertTrue(zipContains(bytes, "word/document.xml"));

        String documentXml = readZipEntry(bytes, "word/document.xml");

        assertTrue(documentXml.contains("UNIVERSIDADE PAULISTA"));
        assertTrue(documentXml.contains("NOME COMPLETO DO ALUNO"));
        assertTrue(documentXml.contains("TÍTULO DO TRABALHO"));
        assertTrue(documentXml.contains("Subtítulo do trabalho"));
        assertTrue(documentXml.contains("Limeira"));
        assertTrue(documentXml.contains("2026"));

        assertTrue(documentXml.contains("lineRule=\"exact\"") || documentXml.contains("w:lineRule=\"exact\""));
        assertFalse(documentXml.contains("w:type=\"page\""));
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

    private static CoverComponent validCover() {
        return new CoverComponent(
                List.of("UNIVERSIDADE PAULISTA"),
                List.of("NOME COMPLETO DO ALUNO"),
                "TÍTULO DO TRABALHO",
                Optional.of("Subtítulo do trabalho"),
                List.of("Limeira", "2026")
        );
    }

    private static DocumentProfile validProfile() {
        return new DocumentProfile(
                "abnt-unip-profile",
                "ABNT UNIP Profile",
                validPageRule(),
                List.of(
                        style("cover.top", true, true),
                        style("cover.author", false, true),
                        style("cover.title", true, true),
                        style("cover.subtitle", false, false),
                        style("cover.bottom", false, false)
                ),
                List.of(validCoverComponentRule())
        );
    }

    private static CoverComponentRule validCoverComponentRule() {
        return new CoverComponentRule(
                "cover",
                new CoverStyleMapping(
                        "cover.top",
                        "cover.author",
                        "cover.title",
                        "cover.subtitle",
                        "cover.bottom"
                ),
                new CoverLayoutRule(
                        BigDecimal.valueOf(30),
                        BigDecimal.valueOf(10),
                        BigDecimal.valueOf(60),
                        1,
                        52
                )
        );
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

    private static StyleRule style(String id, boolean bold, boolean uppercase) {
        return new StyleRule(
                id,
                StyleType.PARAGRAPH,
                "Times New Roman",
                BigDecimal.valueOf(12),
                TextAlignment.CENTER,
                BigDecimal.valueOf(1.5),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                bold,
                false,
                uppercase
        );
    }
}