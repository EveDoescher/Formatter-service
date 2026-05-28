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
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CoverRendererTest {

    private final CoverRenderer renderer = new CoverRenderer();

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
                List.of("Limeira", "2026")
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
                List.of("Limeira", "2026")
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
                        BigDecimal.valueOf(60)
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
