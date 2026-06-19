package com.abntbuilder.formatter.rendering.component.titlepage;

import com.abntbuilder.formatter.document.component.titlepage.AcademicPerson;
import com.abntbuilder.formatter.document.component.titlepage.TitlePageComponent;
import com.abntbuilder.formatter.document.component.titlepage.TitlePageNature;
import com.abntbuilder.formatter.output.docx.api.DocxBlankLine;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.config.ClasspathJsonProfileProvider;
import com.abntbuilder.formatter.rendering.layout.singlepage.HorizontalPlacementResolver;
import com.abntbuilder.formatter.rendering.layout.singlepage.MarginBasedSinglePageSafetyPolicy;
import com.abntbuilder.formatter.rendering.layout.singlepage.OrderedLayoutGapResolver;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageGapDistributor;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutEngine;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutLineMetrics;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutRenderer;
import com.abntbuilder.formatter.rendering.layout.text.FontMetricsTextMeasurer;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TitlePageRendererTest {

    private final DocumentProfile profile = new ClasspathJsonProfileProvider().findById("abnt-unip-profile");
    private final TitlePageRenderer renderer = titlePageRenderer();

    @Test
    void shouldRenderTitlePageComponentAsDocxBlocks() {
        List<DocxBlock> blocks = renderer.render(validTitlePage(), profile);

        List<DocxParagraph> paragraphs = paragraphs(blocks);
        List<String> paragraphTexts = paragraphs.stream()
                .map(p -> p.runs().get(0).text())
                .toList();

        assertTrue(paragraphTexts.contains("PESSOA AUTORA TESTE 01"));
        assertTrue(paragraphTexts.contains("TITULO DO TRABALHO"));
        assertTrue(paragraphTexts.contains("Subtitulo do trabalho"));
        assertTrue(String.join(" ", paragraphTexts).contains("Trabalho de conclusao de curso"));
        assertTrue(String.join(" ", paragraphTexts).contains("Orientador(a): Prof. Dr. Pessoa Orientadora Teste."));
        assertTrue(paragraphTexts.contains("Limeira"));
        assertTrue(paragraphTexts.contains("2026"));

        DocxParagraph nature = paragraphs.stream()
                .filter(paragraph -> paragraph.runs().get(0).text().contains("Trabalho de conclusao de curso"))
                .findFirst()
                .orElseThrow();

        assertTrue(nature.layoutOverride().orElseThrow().leftIndentCm().orElseThrow().compareTo(BigDecimal.ZERO) > 0);
        assertEquals(0, nature.exactLineHeightPt().orElseThrow().compareTo(BigDecimal.valueOf(12)));
        assertTrue(blocks.stream().anyMatch(DocxBlankLine.class::isInstance));
        assertTrue(paragraphs.stream().allMatch(paragraph -> paragraph.spacingBeforeOverridePt().isEmpty()));
        assertTrue(paragraphs.stream().allMatch(paragraph -> paragraph.exactLineHeightPt().isPresent()));
    }

    @Test
    void shouldRenderTitlePageWithoutSubtitleAdvisorOrCoadvisor() {
        TitlePageComponent titlePage = new TitlePageComponent(
                List.of("Pessoa Autora Teste 01"),
                "Titulo do Trabalho",
                Optional.empty(),
                nature(),
                Optional.empty(),
                Optional.empty(),
                "Limeira",
                "2026"
        );

        List<String> paragraphTexts = paragraphs(renderer.render(titlePage, profile))
                .stream()
                .map(p -> p.runs().get(0).text())
                .toList();

        assertTrue(paragraphTexts.contains("PESSOA AUTORA TESTE 01"));
        assertTrue(paragraphTexts.contains("TITULO DO TRABALHO"));
        assertTrue(paragraphTexts.stream().noneMatch(text -> text.contains("Subtitulo")));
        assertTrue(paragraphTexts.stream().noneMatch(text -> text.contains("Orientador")));
        assertTrue(paragraphTexts.stream().noneMatch(text -> text.contains("Coorientador")));
    }

    private static List<DocxParagraph> paragraphs(List<DocxBlock> blocks) {
        return blocks.stream()
                .filter(DocxParagraph.class::isInstance)
                .map(DocxParagraph.class::cast)
                .toList();
    }

    private static TitlePageComponent validTitlePage() {
        return new TitlePageComponent(
                List.of("Pessoa Autora Teste 01"),
                "Titulo do Trabalho",
                Optional.of("Subtitulo do trabalho"),
                nature(),
                Optional.of(new AcademicPerson("Pessoa Orientadora Teste", Optional.of("Prof. Dr."))),
                Optional.empty(),
                "Limeira",
                "2026"
        );
    }

    private static TitlePageNature nature() {
        return new TitlePageNature(
                "Trabalho de conclusao de curso",
                "obtencao do titulo de graduacao",
                "Analise e Desenvolvimento de Sistemas",
                "Universidade Paulista - UNIP"
        );
    }

    private static TitlePageRenderer titlePageRenderer() {
        return new TitlePageRenderer(
                new TitlePageLayoutCalculator(
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
                ),
                new SinglePageLayoutRenderer()
        );
    }
}
