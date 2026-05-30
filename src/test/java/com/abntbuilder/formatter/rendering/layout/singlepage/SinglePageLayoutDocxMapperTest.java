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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SinglePageLayoutDocxMapperTest {

    private final SinglePageLayoutDocxMapper mapper = mapper();

    @Test
    void shouldMapGroupsUsingSharedSafeLineCapacity() {
        List<DocxBlock> blocks = mapper.mapToDocxBlocksAnchoringLastGroup(
                validPageRule(),
                List.of(
                        new SinglePageLayoutGroup(
                                "cover.title",
                                List.of(item("title", "Titulo"))
                        ),
                        new SinglePageLayoutGroup(
                                "cover.bottom",
                                List.of(item("year", "2026"))
                        )
                ),
                List.of(BigDecimal.ONE)
        );

        assertEquals(31, blocks.size());
        assertEquals(29, blocks.stream().filter(DocxBlankLine.class::isInstance).count());
        assertEquals(2, blocks.stream().filter(DocxParagraph.class::isInstance).count());
        assertTrue(blocks.stream()
                .filter(DocxParagraph.class::isInstance)
                .map(DocxParagraph.class::cast)
                .allMatch(paragraph -> paragraph.exactLineHeightPt().orElseThrow().compareTo(BigDecimal.valueOf(18)) == 0));
        assertTrue(blocks.stream()
                .filter(DocxBlankLine.class::isInstance)
                .map(DocxBlankLine.class::cast)
                .allMatch(blankLine -> blankLine.exactLineHeightPt().orElseThrow().compareTo(BigDecimal.valueOf(18)) == 0));
    }

    @Test
    void shouldFailWhenContentDoesNotFitRenderablePageArea() {
        SinglePageLayoutGroup group = new SinglePageLayoutGroup(
                "cover.title",
                List.of(
                        item("title.1", "Linha um"),
                        item("title.2", "Linha dois")
                )
        );

        assertThrows(
                SinglePageLayoutOverflowException.class,
                () -> mapper.mapToDocxBlocksAnchoringLastGroup(
                        pageWithTwoUsableLineSlots(),
                        List.of(group),
                        List.of()
                )
        );
    }

    @Test
    void shouldUseSharedGapDistributorRules() {
        List<DocxBlock> blocks = mapper.mapToDocxBlocksAnchoringLastGroup(
                validPageRule(),
                List.of(
                        new SinglePageLayoutGroup(
                                "cover.top",
                                List.of(item("institution", "Universidade"))
                        ),
                        new SinglePageLayoutGroup(
                                "cover.title",
                                List.of(item("title", "Titulo"))
                        ),
                        new SinglePageLayoutGroup(
                                "cover.bottom",
                                List.of(item("year", "2026"))
                        )
                ),
                List.of(BigDecimal.valueOf(1), BigDecimal.valueOf(100))
        );

        assertEquals(DocxBlankLine.class, blocks.get(1).getClass());
        assertEquals(28, blocks.stream().filter(DocxBlankLine.class::isInstance).count());
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

    private static PageRule pageWithTwoUsableLineSlots() {
        return new PageRule(
                BigDecimal.valueOf(21),
                BigDecimal.valueOf(5.3),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(3),
                PageOrientation.PORTRAIT
        );
    }

    private static StyleRule validStyle() {
        return new StyleRule(
                "cover.title",
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
                true,
                false,
                true
        );
    }

    private static SinglePageLayoutItem item(String id, String text) {
        return new SinglePageLayoutItem(id, validStyle(), List.of(text));
    }

    private static SinglePageLayoutDocxMapper mapper() {
        return new SinglePageLayoutDocxMapper(
                new SinglePageLayoutEngine(
                        new SinglePageLayoutLineMetrics(),
                        new MarginBasedSinglePageSafetyPolicy(),
                        new SinglePageGapDistributor()
                ),
                new SinglePageLayoutRenderer()
        );
    }
}
