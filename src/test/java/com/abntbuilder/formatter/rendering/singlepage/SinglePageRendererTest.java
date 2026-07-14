package com.abntbuilder.formatter.rendering.singlepage;

import com.abntbuilder.formatter.engine.model.content.singlepage.ComposedTextValue;
import com.abntbuilder.formatter.engine.model.content.singlepage.SinglePageContent;
import com.abntbuilder.formatter.engine.model.content.singlepage.TextListValue;
import com.abntbuilder.formatter.engine.model.content.singlepage.TextValue;
import com.abntbuilder.formatter.engine.model.output.DocxBlankLine;
import com.abntbuilder.formatter.engine.model.output.DocxBlock;
import com.abntbuilder.formatter.engine.model.output.DocxParagraph;
import com.abntbuilder.formatter.engine.model.profile.DocumentProfile;
import com.abntbuilder.formatter.engine.model.profile.PageOrientation;
import com.abntbuilder.formatter.engine.model.profile.PageRule;
import com.abntbuilder.formatter.engine.model.profile.StyleRule;
import com.abntbuilder.formatter.engine.model.profile.StyleType;
import com.abntbuilder.formatter.engine.model.profile.TextAlignment;
import com.abntbuilder.formatter.engine.model.profile.component.singlepage.ComposedTextSlotRule;
import com.abntbuilder.formatter.engine.model.profile.component.singlepage.SinglePageComponentRule;
import com.abntbuilder.formatter.engine.model.profile.component.singlepage.TextListSlotRule;
import com.abntbuilder.formatter.engine.model.profile.component.singlepage.TextSlotRule;
import com.abntbuilder.formatter.engine.model.profile.layout.singlepage.LayoutGapRule;
import com.abntbuilder.formatter.engine.model.profile.layout.singlepage.SinglePageGroupRule;
import com.abntbuilder.formatter.engine.model.profile.layout.singlepage.SinglePageItemRule;
import com.abntbuilder.formatter.engine.model.profile.layout.singlepage.SinglePageLayoutPolicy;
import com.abntbuilder.formatter.engine.model.profile.layout.singlepage.SinglePageLayoutRule;
import com.abntbuilder.formatter.rendering.singlepage.MarginBasedSinglePageSafetyPolicy;
import com.abntbuilder.formatter.rendering.singlepage.OrderedLayoutGapResolver;
import com.abntbuilder.formatter.rendering.singlepage.SinglePageGapDistributor;
import com.abntbuilder.formatter.rendering.singlepage.SinglePageLayoutEngine;
import com.abntbuilder.formatter.rendering.singlepage.SinglePageLayoutLineMetrics;
import com.abntbuilder.formatter.rendering.singlepage.SinglePageLayoutRenderer;
import com.abntbuilder.formatter.rendering.text.FontMetricsTextMeasurer;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SinglePageRendererTest {

    private final SinglePageRenderer renderer = renderer("cover");

    @Test
    void shouldReturnComponentId() {
        assertEquals("cover", renderer.componentId());
        assertEquals(SinglePageContent.class, renderer.componentType());
    }

    @Test
    void shouldRenderTextAndTextListSlotsAsDocxParagraphs() {
        SinglePageContent content = new SinglePageContent("cover", Map.of(
                "title", new TextValue("Título do Trabalho"),
                "authors", new TextListValue(List.of("Ana Souza", "Carlos Lima")),
                "city", new TextValue("Limeira"),
                "year", new TextValue("2026")
        ));

        List<DocxBlock> blocks = renderer.render(content, profile("cover", coverRule()));

        List<DocxParagraph> paragraphs = paragraphs(blocks);
        assertEquals(5, paragraphs.size());
        assertEquals("Título do Trabalho", paragraphs.get(0).runs().get(0).text());
        assertEquals("Ana Souza", paragraphs.get(1).runs().get(0).text());
        assertEquals("Carlos Lima", paragraphs.get(2).runs().get(0).text());
        assertEquals("Limeira", paragraphs.get(3).runs().get(0).text());
        assertEquals("2026", paragraphs.get(4).runs().get(0).text());
        assertTrue(blocks.stream().anyMatch(DocxBlankLine.class::isInstance));
    }

    @Test
    void shouldResolveComposedTextSlotViaTemplate() {
        SinglePageComponentRule rule = ruleWithNature();
        SinglePageContent content = new SinglePageContent("cover", Map.of(
                "nature", new ComposedTextValue(Map.of(
                        "workType", "TCC",
                        "courseName", "ADS"
                )),
                "city", new TextValue("Limeira"),
                "year", new TextValue("2026")
        ));

        List<DocxBlock> blocks = renderer("cover").render(content, profile("cover", rule));

        List<DocxParagraph> paragraphs = paragraphs(blocks);
        assertTrue(paragraphs.stream()
                .anyMatch(p -> p.runs().get(0).text().equals("TCC em ADS.")));
    }

    @Test
    void shouldSkipOptionalSlotWhenAbsent() {
        SinglePageContent content = new SinglePageContent("cover", Map.of(
                "title", new TextValue("Título"),
                "city", new TextValue("Limeira"),
                "year", new TextValue("2026")
        ));

        List<DocxBlock> blocks = renderer.render(content, profile("cover", coverRule()));

        List<DocxParagraph> paragraphs = paragraphs(blocks);
        // authors absent → only title + city + year = 3 paragraphs
        assertEquals(3, paragraphs.size());
    }

    @Test
    void shouldRenderWithSpacerLinesBetweenGroups() {
        SinglePageContent content = new SinglePageContent("cover", Map.of(
                "title", new TextValue("Título"),
                "city", new TextValue("Limeira"),
                "year", new TextValue("2026")
        ));

        List<DocxBlock> blocks = renderer.render(content, profile("cover", coverRule()));

        assertTrue(blocks.stream().anyMatch(DocxBlankLine.class::isInstance));
        assertTrue(blocks.stream().anyMatch(DocxParagraph.class::isInstance));
    }

    // --- Fixtures ---

    private static SinglePageRenderer renderer(String componentId) {
        return new SinglePageRenderer(
                componentId,
                new SinglePageLayoutCalculator(
                        new SinglePageContentValidator(),
                        new SinglePageLayoutAssembler(
                                new FontMetricsTextMeasurer(),
                                new OrderedLayoutGapResolver()
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

    private static SinglePageComponentRule coverRule() {
        return new SinglePageComponentRule(
                "cover",
                true,
                null,
                Map.of(
                        "title", new TextSlotRule(true, null, null),
                        "authors", new TextListSlotRule(false, null, null),
                        "city", new TextSlotRule(true, null, null),
                        "year", new TextSlotRule(true, null, null)
                ),
                Map.of(
                        "title", "sp.title",
                        "authors", "sp.authors",
                        "city", "sp.bottom",
                        "year", "sp.bottom"
                ),
                new SinglePageLayoutRule(
                        List.of(
                                new SinglePageGroupRule("top", true, List.of(
                                        new SinglePageItemRule("title", true, Optional.empty()),
                                        new SinglePageItemRule("authors", false, Optional.empty())
                                )),
                                new SinglePageGroupRule("bottom", true, List.of(
                                        new SinglePageItemRule("city", true, Optional.of(1)),
                                        new SinglePageItemRule("year", true, Optional.of(1))
                                ))
                        ),
                        List.of(new LayoutGapRule("top", "bottom", BigDecimal.valueOf(60))),
                        SinglePageLayoutPolicy.defaultSinglePagePolicy()
                )
        );
    }

    private static SinglePageComponentRule ruleWithNature() {
        return new SinglePageComponentRule(
                "cover",
                true,
                null,
                Map.of(
                        "nature", new ComposedTextSlotRule(true, null, null, "{workType} em {courseName}.", List.of("workType", "courseName")),
                        "city", new TextSlotRule(true, null, null),
                        "year", new TextSlotRule(true, null, null)
                ),
                Map.of(
                        "nature", "sp.title",
                        "city", "sp.bottom",
                        "year", "sp.bottom"
                ),
                new SinglePageLayoutRule(
                        List.of(
                                new SinglePageGroupRule("top", true, List.of(
                                        new SinglePageItemRule("nature", true, Optional.empty())
                                )),
                                new SinglePageGroupRule("bottom", true, List.of(
                                        new SinglePageItemRule("city", true, Optional.of(1)),
                                        new SinglePageItemRule("year", true, Optional.of(1))
                                ))
                        ),
                        List.of(new LayoutGapRule("top", "bottom", BigDecimal.valueOf(60))),
                        SinglePageLayoutPolicy.defaultSinglePagePolicy()
                )
        );
    }

    private static DocumentProfile profile(String componentId, SinglePageComponentRule rule) {
        return new DocumentProfile(
                "test-profile", "Test Profile",
                pageRule(),
                List.of(style("sp.title"), style("sp.authors"), style("sp.bottom")),
                List.of(rule),
                List.of(componentId)
        );
    }

    private static StyleRule style(String id) {
        return new StyleRule(
                id, StyleType.PARAGRAPH, "Times New Roman", BigDecimal.valueOf(12),
                TextAlignment.CENTER, BigDecimal.valueOf(1.5),
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO,
                false, false, false
        );
    }

    private static PageRule pageRule() {
        return new PageRule(
                BigDecimal.valueOf(21), BigDecimal.valueOf(29.7),
                BigDecimal.valueOf(3), BigDecimal.valueOf(2),
                BigDecimal.valueOf(2), BigDecimal.valueOf(3),
                PageOrientation.PORTRAIT
        );
    }

    private static List<DocxParagraph> paragraphs(List<DocxBlock> blocks) {
        return blocks.stream()
                .filter(DocxParagraph.class::isInstance)
                .map(DocxParagraph.class::cast)
                .toList();
    }
}
