package com.abntbuilder.formatter.rendering.component.titlepage;

import com.abntbuilder.formatter.document.component.titlepage.AcademicPerson;
import com.abntbuilder.formatter.document.component.titlepage.TitlePageComponent;
import com.abntbuilder.formatter.document.component.titlepage.TitlePageNature;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxDocument;
import com.abntbuilder.formatter.output.docx.docx4j.Docx4jWriter;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.resolution.ClasspathJsonProfileProvider;
import com.abntbuilder.formatter.rendering.layout.singlepage.HorizontalPlacementResolver;
import com.abntbuilder.formatter.rendering.layout.singlepage.MarginBasedSinglePageSafetyPolicy;
import com.abntbuilder.formatter.rendering.layout.singlepage.OrderedLayoutGapResolver;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageGapDistributor;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutEngine;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutLineMetrics;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutRenderer;
import com.abntbuilder.formatter.rendering.layout.text.FontMetricsTextMeasurer;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TitlePageRendererDocxSanityTest {

    @Test
    void shouldGenerateTitlePageDocxXmlWithRightHalfNatureBlockAndMixedLineSpacing() throws IOException {
        DocumentProfile profile = new ClasspathJsonProfileProvider().findById("abnt-unip-profile");
        TitlePageComponent titlePage = validTitlePage();
        TitlePageRenderer renderer = new TitlePageRenderer(
                titlePageLayoutCalculator(),
                new SinglePageLayoutRenderer()
        );

        List<DocxBlock> blocks = renderer.render(titlePage, profile);
        byte[] bytes = new Docx4jWriter().write(new DocxDocument(profile.pageRule(), blocks));

        assertTrue(bytes.length > 0);
        assertTrue(zipContains(bytes, "[Content_Types].xml"));
        assertTrue(zipContains(bytes, "word/document.xml"));

        String documentXml = readZipEntry(bytes, "word/document.xml");

        assertTrue(documentXml.contains("NOME COMPLETO DO ALUNO"));
        assertTrue(documentXml.contains("TITULO DO TRABALHO"));
        assertTrue(documentXml.contains("Subtitulo do trabalho"));
        assertTrue(documentXml.contains("Universidade Paulista - UNIP"));
        assertTrue(documentXml.contains("Orientador(a): Prof. Dr."));
        assertTrue(documentXml.contains("Jose da Silva."));
        assertTrue(documentXml.contains("Limeira"));
        assertTrue(documentXml.contains("2026"));

        assertTrue(documentXml.contains("w:left=\""
                + MeasurementConverter.centimetersToTwips(BigDecimal.valueOf(8))
                + "\""));
        assertTrue(documentXml.contains("w:line=\"240\""));
        assertTrue(documentXml.contains("w:line=\"360\""));
        assertEquals(blocks.size(), countParagraphs(documentXml));

        assertAppearsBefore(documentXml, "NOME COMPLETO DO ALUNO", "TITULO DO TRABALHO");
        assertAppearsBefore(documentXml, "TITULO DO TRABALHO", "Universidade Paulista - UNIP");
        assertAppearsBefore(documentXml, "Universidade Paulista - UNIP", "Limeira");
        assertAppearsBefore(documentXml, "Limeira", "2026");

        assertFalse(documentXml.contains("w:type=\"page\""));
        assertFalse(documentXml.contains("<w:br"));
    }

    private static TitlePageComponent validTitlePage() {
        return new TitlePageComponent(
                List.of("Nome Completo do Aluno"),
                "Titulo do Trabalho",
                Optional.of("Subtitulo do trabalho"),
                new TitlePageNature(
                        "Trabalho de conclusao de curso",
                        "obtencao do titulo de graduacao",
                        "Analise e Desenvolvimento de Sistemas",
                        "Universidade Paulista - UNIP"
                ),
                Optional.of(new AcademicPerson("Jose da Silva", Optional.of("Prof. Dr."))),
                Optional.empty(),
                "Limeira",
                "2026"
        );
    }

    private static TitlePageLayoutCalculator titlePageLayoutCalculator() {
        return new TitlePageLayoutCalculator(
                new TitlePageLayoutAssembler(
                        new FontMetricsTextMeasurer(),
                        new OrderedLayoutGapResolver(),
                        new TitlePageProfileContentValidator(),
                        new TitlePageTextTemplateResolver(),
                        new HorizontalPlacementResolver()
                ),
                new SinglePageLayoutEngine(
                        new SinglePageLayoutLineMetrics(),
                        new MarginBasedSinglePageSafetyPolicy(),
                        new SinglePageGapDistributor()
                )
        );
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
}
