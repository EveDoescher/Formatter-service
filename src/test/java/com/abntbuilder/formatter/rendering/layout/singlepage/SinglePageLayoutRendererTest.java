package com.abntbuilder.formatter.rendering.layout.singlepage;

import com.abntbuilder.formatter.output.docx.api.DocxBlankLine;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.StyleType;
import com.abntbuilder.formatter.profile.model.TextAlignment;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SinglePageLayoutRendererTest {

    private final SinglePageLayoutRenderer renderer = new SinglePageLayoutRenderer();

    @Test
    void shouldRenderTextLinesAndSpacerLinesWithExactLineHeight() {
        StyleRule style = style();
        BigDecimal exactLineHeightPt = BigDecimal.valueOf(18);
        SinglePageLayoutPlan plan = new SinglePageLayoutPlan(
                List.of(
                        new SinglePageTextLines("cover.title", "title", style, List.of("Titulo", "Linha dois")),
                        new SinglePageSpacerLines("cover.title->cover.bottom", "cover.title", "cover.bottom", 1, style),
                        new SinglePageTextLines("cover.bottom", "year", style, List.of("2026"))
                ),
                4,
                4,
                exactLineHeightPt,
                new SinglePageLayoutDiagnostic(
                        new SinglePageRenderableArea(4, 0, 4),
                        3,
                        1,
                        Map.of("cover.title", 2, "cover.bottom", 1),
                        Map.of("cover.title.title", 2, "cover.bottom.year", 1),
                        Map.of("cover.title->cover.bottom", 1),
                        exactLineHeightPt
                )
        );

        List<DocxBlock> blocks = renderer.render(plan);

        assertEquals(3, blocks.size());
        assertEquals(2, blocks.stream().filter(DocxParagraph.class::isInstance).count());
        assertEquals(1, blocks.stream().filter(DocxBlankLine.class::isInstance).count());
        assertEquals(
                "Titulo Linha dois",
                blocks.stream()
                        .filter(DocxParagraph.class::isInstance)
                        .map(DocxParagraph.class::cast)
                        .findFirst()
                        .orElseThrow()
                        .text()
        );
        assertTrue(blocks.stream()
                .filter(DocxParagraph.class::isInstance)
                .map(DocxParagraph.class::cast)
                .allMatch(paragraph -> paragraph.exactLineHeightPt()
                        .orElseThrow()
                        .compareTo(exactLineHeightPt) == 0));
        assertTrue(blocks.stream()
                .filter(DocxBlankLine.class::isInstance)
                .map(DocxBlankLine.class::cast)
                .allMatch(blankLine -> blankLine.exactLineHeightPt()
                        .orElseThrow()
                        .compareTo(exactLineHeightPt) == 0));
    }

    private static StyleRule style() {
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
                false,
                false,
                false
        );
    }
}
