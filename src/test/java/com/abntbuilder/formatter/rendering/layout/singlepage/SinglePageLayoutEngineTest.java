package com.abntbuilder.formatter.rendering.layout.singlepage;

import com.abntbuilder.formatter.output.docx.api.DocxBlankLine;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.profile.model.PageOrientation;
import com.abntbuilder.formatter.profile.model.PageRule;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.StyleType;
import com.abntbuilder.formatter.profile.model.TextAlignment;
import com.abntbuilder.formatter.shared.exception.SinglePageLayoutOverflowException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SinglePageLayoutDocxMapperTest {

    private final SinglePageLayoutDocxMapper mapper = new SinglePageLayoutDocxMapper();

    @Test
    void shouldMapGroupsToDocxBlocksUsingWeightedGaps() {
        List<DocxBlock> blocks = mapper.mapToDocxBlocks(
                validPageRule(),
                List.of(
                        group("cover.top", "UNIVERSIDADE PAULISTA"),
                        group("cover.authors", "NOME DO ALUNO"),
                        group("cover.title", "TÍTULO DO TRABALHO", "Subtítulo do trabalho"),
                        group("cover.bottom", "Limeira", "2026")
                ),
                List.of(
                        BigDecimal.valueOf(45),
                        BigDecimal.valueOf(15),
                        BigDecimal.valueOf(40)
                ),
                0
        );

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
        assertTrue(paragraphs.stream().allMatch(paragraph -> paragraph.exactLineHeightPt().isPresent()));
    }

    @Test
    void shouldRejectGapWeightsSizeDifferentFromGroupGapCount() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> mapper.mapToDocxBlocks(
                        validPageRule(),
                        List.of(
                                group("cover.top", "UNIVERSIDADE PAULISTA"),
                                group("cover.bottom", "Limeira", "2026")
                        ),
                        List.of(
                                BigDecimal.valueOf(45),
                                BigDecimal.valueOf(40)
                        ),
                        0
                )
        );

        assertEquals("gapWeights size must be equal to groups size minus one.", exception.getMessage());
    }

    @Test
    void shouldRejectNonPositiveGapWeight() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> mapper.mapToDocxBlocks(
                        validPageRule(),
                        List.of(
                                group("cover.top", "UNIVERSIDADE PAULISTA"),
                                group("cover.bottom", "Limeira", "2026")
                        ),
                        List.of(BigDecimal.ZERO),
                        0
                )
        );

        assertEquals("gapWeights must contain only positive values.", exception.getMessage());
    }

    @Test
    void shouldRejectNegativeSafetyBlankLines() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> mapper.mapToDocxBlocks(
                        validPageRule(),
                        List.of(
                                group("cover.top", "UNIVERSIDADE PAULISTA"),
                                group("cover.bottom", "Limeira", "2026")
                        ),
                        List.of(BigDecimal.ONE),
                        -1
                )
        );

        assertEquals("safetyBlankLines must not be negative.", exception.getMessage());
    }

    @Test
    void shouldThrowOverflowWhenContentDoesNotFit() {
        PageRule tinyPage = new PageRule(
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(0.5),
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(0.5),
                BigDecimal.valueOf(1),
                PageOrientation.PORTRAIT
        );

        assertThrows(
                SinglePageLayoutOverflowException.class,
                () -> mapper.mapToDocxBlocks(
                        tinyPage,
                        List.of(
                                group("cover.top", "UNIVERSIDADE PAULISTA"),
                                group("cover.authors", "NOME DO ALUNO"),
                                group("cover.title", "TÍTULO DO TRABALHO", "Subtítulo do trabalho"),
                                group("cover.bottom", "Limeira", "2026")
                        ),
                        List.of(
                                BigDecimal.valueOf(45),
                                BigDecimal.valueOf(15),
                                BigDecimal.valueOf(40)
                        ),
                        0
                )
        );
    }

    private static SinglePageLayoutGroup group(String id, String... texts) {
        return new SinglePageLayoutGroup(
                id,
                List.of(texts)
                        .stream()
                        .map(text -> new SinglePageLayoutTextLine(text, validStyle()))
                        .toList()
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

    private static StyleRule validStyle() {
        return new StyleRule(
                "cover.default",
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
                false,
                false,
                false
        );
    }
}