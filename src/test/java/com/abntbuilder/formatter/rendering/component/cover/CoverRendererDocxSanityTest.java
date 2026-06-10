package com.abntbuilder.formatter.rendering.component.cover;

import com.abntbuilder.formatter.document.component.cover.CoverComponent;
import com.abntbuilder.formatter.output.docx.api.DocxBlankLine;
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
import com.abntbuilder.formatter.profile.model.layout.singlepage.LayoutGapRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageGroupRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageItemRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageLayoutPolicy;
import com.abntbuilder.formatter.rendering.component.cover.layout.CoverLayoutAssembler;
import com.abntbuilder.formatter.rendering.component.cover.layout.CoverLayoutCalculator;
import com.abntbuilder.formatter.rendering.component.cover.layout.CoverLayoutPlan;
import com.abntbuilder.formatter.rendering.component.cover.layout.CoverProfileContentValidator;
import com.abntbuilder.formatter.rendering.layout.singlepage.MarginBasedSinglePageSafetyPolicy;
import com.abntbuilder.formatter.rendering.layout.singlepage.OrderedLayoutGapResolver;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageGapDistributor;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutEngine;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutLineMetrics;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutRenderer;
import com.abntbuilder.formatter.rendering.layout.text.FontMetricsTextMeasurer;
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
        CoverComponent cover = validCover();
        CoverLayoutCalculator calculator = coverLayoutCalculator();
        CoverRenderer renderer = new CoverRenderer(calculator, new SinglePageLayoutRenderer());
        CoverLayoutPlan plan = calculator.calculate(cover, profile);

        List<DocxBlock> blocks = renderer.render(cover, profile);

        byte[] bytes = new Docx4jWriter().write(new DocxDocument(
                profile.pageRule(),
                blocks
        ));

        assertTrue(bytes.length > 0);
        assertTrue(zipContains(bytes, "[Content_Types].xml"));
        assertTrue(zipContains(bytes, "word/document.xml"));

        String documentXml = readZipEntry(bytes, "word/document.xml");

        assertTrue(documentXml.contains(cover.institutionalLines().getFirst()));
        assertTrue(documentXml.contains(cover.authors().getFirst()));
        assertTrue(documentXml.contains(cover.title()));
        assertTrue(documentXml.contains(cover.subtitle().orElseThrow()));
        assertTrue(documentXml.contains(cover.city()));
        assertTrue(documentXml.contains(cover.year()));

        assertEquals(plan.totalLines(), blocks.size());
        assertEquals(blocks.size(), countParagraphs(documentXml));
        assertEquals(blocks.size(), countOccurrences(documentXml, "w:lineRule=\"exact\""));
        assertTrue(countOccurrences(documentXml, "w:line=\"360\"") > 0);
        assertEquals(
                countBlocks(blocks, DocxBlankLine.class),
                countOccurrences(documentXml, "<w:t xml:space=\"preserve\"> </w:t>")
        );

        assertAppearsBefore(documentXml, cover.institutionalLines().getFirst(), cover.authors().getFirst());
        assertAppearsBefore(documentXml, cover.authors().getFirst(), cover.title());
        assertAppearsBefore(documentXml, cover.title(), cover.city());
        assertAppearsBefore(documentXml, cover.city(), cover.year());

        assertFalse(documentXml.contains("w:type=\"page\""));
        assertFalse(documentXml.contains("<w:br"));
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

    private static long countBlocks(List<DocxBlock> blocks, Class<? extends DocxBlock> blockType) {
        return blocks.stream()
                .filter(blockType::isInstance)
                .count();
    }

    private static int countOccurrences(String text, String pattern) {
        int count = 0;
        int index = 0;

        while ((index = text.indexOf(pattern, index)) >= 0) {
            count++;
            index += pattern.length();
        }

        return count;
    }

    private static int countParagraphs(String documentXml) {
        return countOccurrences(documentXml, "<w:p>")
                + countOccurrences(documentXml, "<w:p ");
    }

    private static void assertAppearsBefore(String documentXml, String firstText, String secondText) {
        int firstIndex = documentXml.indexOf(firstText);
        int secondIndex = documentXml.indexOf(secondText);

        assertTrue(firstIndex >= 0, "Missing expected text in document XML: " + firstText);
        assertTrue(secondIndex >= 0, "Missing expected text in document XML: " + secondText);
        assertTrue(firstIndex < secondIndex, firstText + " should appear before " + secondText);
    }

    private static CoverComponent validCover() {
        return new CoverComponent(
                List.of("UNIVERSIDADE PAULISTA"),
                List.of("PESSOA AUTORA TESTE 01"),
                "TITULO DO TRABALHO",
                Optional.of("Subtitulo do trabalho"),
                "Limeira",
                "2026"
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
                List.of(validCoverComponentRule()),
                List.of("cover", DocumentProfile.PARAGRAPHS_INTERNAL_COMPONENT_ID)
        );
    }

    private static CoverComponentRule validCoverComponentRule() {
        return new CoverComponentRule(
                "cover",
                new com.abntbuilder.formatter.profile.model.component.ComponentContentBindings(java.util.Map.of()),
                new CoverStyleMapping(
                        "cover.top",
                        "cover.author",
                        "cover.title",
                        "cover.subtitle",
                        "cover.bottom",
                        "cover.bottom"
                ),
                validCoverLayoutRule()
        );
    }

    private static CoverLayoutRule validCoverLayoutRule() {
        return new CoverLayoutRule(
                List.of(
                        new SinglePageGroupRule(
                                CoverLayoutRule.INSTITUTION_GROUP_ID,
                                true,
                                List.of(new SinglePageItemRule("institutionalLines", true, Optional.empty()))
                        ),
                        new SinglePageGroupRule(
                                CoverLayoutRule.AUTHORS_GROUP_ID,
                                false,
                                List.of(new SinglePageItemRule("authors", false, Optional.empty()))
                        ),
                        new SinglePageGroupRule(
                                CoverLayoutRule.TITLE_GROUP_ID,
                                true,
                                List.of(
                                        new SinglePageItemRule("title", true, Optional.empty()),
                                        new SinglePageItemRule("subtitle", false, Optional.empty())
                                )
                        ),
                        new SinglePageGroupRule(
                                CoverLayoutRule.BOTTOM_GROUP_ID,
                                true,
                                List.of(
                                        new SinglePageItemRule("city", true, Optional.of(1)),
                                        new SinglePageItemRule("year", true, Optional.of(1))
                                )
                        )
                ),
                List.of(
                        new LayoutGapRule(CoverLayoutRule.INSTITUTION_GROUP_ID, CoverLayoutRule.AUTHORS_GROUP_ID, BigDecimal.valueOf(30)),
                        new LayoutGapRule(CoverLayoutRule.AUTHORS_GROUP_ID, CoverLayoutRule.TITLE_GROUP_ID, BigDecimal.valueOf(10)),
                        new LayoutGapRule(CoverLayoutRule.TITLE_GROUP_ID, CoverLayoutRule.BOTTOM_GROUP_ID, BigDecimal.valueOf(60))
                ),
                SinglePageLayoutPolicy.defaultSinglePagePolicy()
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

    private static CoverLayoutCalculator coverLayoutCalculator() {
        return new CoverLayoutCalculator(
                new CoverLayoutAssembler(
                        new FontMetricsTextMeasurer(),
                        new OrderedLayoutGapResolver(),
                        new CoverProfileContentValidator()
                ),
                new SinglePageLayoutEngine(
                        new SinglePageLayoutLineMetrics(),
                        new MarginBasedSinglePageSafetyPolicy(),
                        new SinglePageGapDistributor()
                )
        );
    }
}
