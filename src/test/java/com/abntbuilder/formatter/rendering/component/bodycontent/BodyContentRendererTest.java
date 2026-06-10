package com.abntbuilder.formatter.rendering.component.bodycontent;

import com.abntbuilder.formatter.document.component.bodycontent.BodyContentComponent;
import com.abntbuilder.formatter.document.component.bodycontent.BodySection;
import com.abntbuilder.formatter.output.docx.api.DocxBlankLine;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxPageBreak;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.PageOrientation;
import com.abntbuilder.formatter.profile.model.PageRule;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.StyleType;
import com.abntbuilder.formatter.profile.model.TextAlignment;
import com.abntbuilder.formatter.profile.model.component.bodycontent.BodyContentComponentRule;
import com.abntbuilder.formatter.profile.model.component.bodycontent.BodyContentLayoutRule;
import com.abntbuilder.formatter.profile.model.component.bodycontent.BodyContentNumberingRule;
import com.abntbuilder.formatter.profile.model.component.bodycontent.BodyContentStyleMapping;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BodyContentRendererTest {

    private final BodyContentRenderer renderer = new BodyContentRenderer();

    @Test
    void shouldRenderSectionTitlesAndParagraphsWithProfileStyles() {
        BodyContentComponent component = new BodyContentComponent(List.of(
                new BodySection(
                        "introducao",
                        1,
                        Optional.of("Introducao"),
                        List.of("Primeiro paragrafo.", "Segundo paragrafo.")
                ),
                new BodySection(
                        "fundamentacao",
                        2,
                        Optional.of("Fundamentacao"),
                        List.of("Paragrafo de fundamentacao.")
                ),
                new BodySection(
                        "detalhe",
                        3,
                        Optional.of("Detalhe"),
                        List.of("Paragrafo de detalhe.")
                )
        ));

        List<DocxParagraph> paragraphs = renderer.render(component, profile())
                .stream()
                .filter(DocxParagraph.class::isInstance)
                .map(DocxParagraph.class::cast)
                .toList();

        assertEquals(
                List.of(
                        "1.0 Introducao",
                        "Primeiro paragrafo.",
                        "Segundo paragrafo.",
                        "1.1 Fundamentacao",
                        "Paragrafo de fundamentacao.",
                        "1.1.1 Detalhe",
                        "Paragrafo de detalhe."
                ),
                paragraphs.stream().map(DocxParagraph::text).toList()
        );
        assertEquals("bodyContent.heading1", paragraphs.get(0).styleRule().id());
        assertEquals("bodyContent.paragraph", paragraphs.get(1).styleRule().id());
        assertEquals("bodyContent.heading2", paragraphs.get(3).styleRule().id());
        assertEquals("bodyContent.heading3", paragraphs.get(5).styleRule().id());
    }

    @Test
    void shouldRenderSectionWithoutTitle() {
        BodyContentComponent component = new BodyContentComponent(List.of(
                new BodySection("sem-titulo", 1, Optional.empty(), List.of("Paragrafo sem titulo."))
        ));

        List<DocxBlock> blocks = renderer.render(component, profile());

        assertEquals(1, blocks.size());
        assertEquals("Paragrafo sem titulo.", ((DocxParagraph) blocks.getFirst()).text());
    }

    @Test
    void shouldRenderProfileDrivenBlankLinesAroundSectionTitles() {
        BodyContentComponent component = new BodyContentComponent(List.of(
                new BodySection("introducao", 1, Optional.of("Introducao"), List.of("Primeiro paragrafo.")),
                new BodySection("desenvolvimento", 1, Optional.of("Desenvolvimento"), List.of("Segundo paragrafo."))
        ));

        List<DocxBlock> blocks = renderer.render(component, profile());

        assertEquals(DocxParagraph.class, blocks.get(0).getClass());
        assertEquals(DocxBlankLine.class, blocks.get(1).getClass());
        assertEquals(DocxParagraph.class, blocks.get(2).getClass());
        assertEquals(DocxBlankLine.class, blocks.get(3).getClass());
        assertEquals(DocxParagraph.class, blocks.get(4).getClass());
        assertEquals(DocxBlankLine.class, blocks.get(5).getClass());
        assertEquals(DocxParagraph.class, blocks.get(6).getClass());
    }

    @Test
    void shouldRenderPageBreakBeforePrimarySectionWhenProfileRequestsIt() {
        BodyContentComponent component = new BodyContentComponent(List.of(
                new BodySection("introducao", 1, Optional.of("Introducao"), List.of("Primeiro paragrafo.")),
                new BodySection("desenvolvimento", 1, Optional.of("Desenvolvimento"), List.of("Segundo paragrafo."))
        ));

        List<DocxBlock> blocks = renderer.render(component, profileWithPrimarySectionPageBreak());

        assertEquals(DocxPageBreak.class, blocks.get(3).getClass());
        assertEquals("2.0 Desenvolvimento", ((DocxParagraph) blocks.get(4)).text());
    }

    private static DocumentProfile profile() {
        return new DocumentProfile(
                "test-profile",
                "Test Profile",
                pageRule(),
                List.of(
                        style("bodyContent.heading1", StyleType.HEADING_1, true, true),
                        style("bodyContent.heading2", StyleType.HEADING_2, true, false),
                        style("bodyContent.heading3", StyleType.HEADING_3, true, false),
                        style("bodyContent.paragraph", false, false)
                ),
                List.of(new BodyContentComponentRule(
                        "bodyContent",
                        new BodyContentStyleMapping(
                                List.of("bodyContent.heading1", "bodyContent.heading2", "bodyContent.heading3"),
                                "bodyContent.paragraph"
                        ),
                        new BodyContentNumberingRule(true, ".", ".0"),
                        new BodyContentLayoutRule(1, 1, false, "bodyContent.paragraph")
                )),
                List.of("bodyContent")
        );
    }

    private static DocumentProfile profileWithPrimarySectionPageBreak() {
        return new DocumentProfile(
                "test-profile",
                "Test Profile",
                pageRule(),
                List.of(
                        style("bodyContent.heading1", StyleType.HEADING_1, true, true),
                        style("bodyContent.heading2", StyleType.HEADING_2, true, false),
                        style("bodyContent.heading3", StyleType.HEADING_3, true, false),
                        style("bodyContent.paragraph", false, false)
                ),
                List.of(new BodyContentComponentRule(
                        "bodyContent",
                        new BodyContentStyleMapping(
                                List.of("bodyContent.heading1", "bodyContent.heading2", "bodyContent.heading3"),
                                "bodyContent.paragraph"
                        ),
                        new BodyContentNumberingRule(true, ".", ".0"),
                        new BodyContentLayoutRule(1, 1, true, "bodyContent.paragraph")
                )),
                List.of("bodyContent")
        );
    }

    private static PageRule pageRule() {
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
        return style(id, StyleType.PARAGRAPH, bold, uppercase);
    }

    private static StyleRule style(String id, StyleType type, boolean bold, boolean uppercase) {
        return new StyleRule(
                id,
                type,
                "Times New Roman",
                BigDecimal.valueOf(12),
                TextAlignment.JUSTIFIED,
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
