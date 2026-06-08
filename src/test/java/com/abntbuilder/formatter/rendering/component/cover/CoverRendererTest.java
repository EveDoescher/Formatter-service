package com.abntbuilder.formatter.rendering.component.cover;

import com.abntbuilder.formatter.document.component.cover.CoverComponent;
import com.abntbuilder.formatter.output.docx.api.DocxBlankLine;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
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
import com.abntbuilder.formatter.rendering.component.cover.layout.CoverProfileContentValidator;
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

import static org.junit.jupiter.api.Assertions.*;

class CoverRendererTest {

    private final CoverRenderer renderer = coverRenderer();

    @Test
    void shouldRenderCoverComponentAsDocxBlocks() {
        List<DocxBlock> blocks = renderer.render(validCover(), validProfile());

        List<DocxParagraph> paragraphs = blocks.stream()
                .filter(DocxParagraph.class::isInstance)
                .map(DocxParagraph.class::cast)
                .toList();

        assertEquals(6, paragraphs.size());

        assertEquals("UNIVERSIDADE PAULISTA", paragraphs.get(0).text());
        assertEquals("NOME DO ALUNO", paragraphs.get(1).text());
        assertEquals("TÍTULO DO TRABALHO", paragraphs.get(2).text());
        assertEquals("Subtítulo do trabalho", paragraphs.get(3).text());
        assertEquals("Limeira", paragraphs.get(4).text());
        assertEquals("2026", paragraphs.get(5).text());

        assertTrue(blocks.stream().anyMatch(DocxBlankLine.class::isInstance));
        assertTrue(paragraphs.stream().allMatch(paragraph -> paragraph.spacingBeforeOverridePt().isEmpty()));
        assertTrue(paragraphs.stream().allMatch(paragraph -> paragraph.exactLineHeightPt().isPresent()));
    }

    @Test
    void shouldRenderCoverWithoutSubtitle() {
        CoverComponent cover = new CoverComponent(
                List.of("UNIVERSIDADE PAULISTA"),
                List.of("NOME DO ALUNO"),
                "TÍTULO DO TRABALHO",
                Optional.empty(),
                "Limeira",
                "2026"
        );

        List<DocxBlock> blocks = renderer.render(cover, validProfile());

        List<DocxParagraph> paragraphs = blocks.stream()
                .filter(DocxParagraph.class::isInstance)
                .map(DocxParagraph.class::cast)
                .toList();

        assertEquals(5, paragraphs.size());
        assertEquals("TÍTULO DO TRABALHO", paragraphs.get(2).text());
        assertTrue(blocks.stream().anyMatch(DocxBlankLine.class::isInstance));
    }

    private static CoverComponent validCover() {
        return new CoverComponent(
                List.of("UNIVERSIDADE PAULISTA"),
                List.of("NOME DO ALUNO"),
                "TÍTULO DO TRABALHO",
                Optional.of("Subtítulo do trabalho"),
                "Limeira",
                "2026"
        );
    }

    private static DocumentProfile validProfile() {
        return new DocumentProfile(
                "test-profile",
                "Test Profile",
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

    private static CoverRenderer coverRenderer() {
        return new CoverRenderer(
                new CoverLayoutCalculator(
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
                ),
                new SinglePageLayoutRenderer()
        );
    }
}
