package com.abntbuilder.formatter.rendering.bodycontent;

import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyParagraph;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodySection;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyFigure;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyImageSource;
import com.abntbuilder.formatter.engine.model.content.bodycontent.ImageSourceType;
import com.abntbuilder.formatter.engine.model.output.DocxBlankLine;
import com.abntbuilder.formatter.engine.model.output.DocxBlock;
import com.abntbuilder.formatter.engine.model.output.DocxPageBreak;
import com.abntbuilder.formatter.engine.model.output.DocxParagraph;
import com.abntbuilder.formatter.engine.model.profile.DocumentProfile;
import com.abntbuilder.formatter.engine.model.profile.PageOrientation;
import com.abntbuilder.formatter.engine.model.profile.PageRule;
import com.abntbuilder.formatter.engine.model.profile.StyleRule;
import com.abntbuilder.formatter.engine.model.profile.StyleType;
import com.abntbuilder.formatter.engine.model.profile.TextAlignment;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.BodyContentComponentRule;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.BodyContentLayoutRule;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.BodyContentNumberingRule;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.BodyContentStyleMapping;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.ChartRule;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.CitationFormattingRule;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.CodeListingRule;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.CrossReferenceLabelsRule;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.DisplayObjectContinuationLabels;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.DisplayObjectSourcePlacement;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.FigureRule;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.FrameRule;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.ImageFitPolicy;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.NumberingStrategy;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.TableRule;
import com.abntbuilder.formatter.input.profile.StyleResolver;
import com.abntbuilder.formatter.rendering.bodycontent.DisplayObjectRenderingState;
import com.abntbuilder.formatter.rendering.phase0.Phase0Index;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class FlowLayoutEngineTest {

    private static final String ONE_PIXEL_PNG =
            "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/p9sAAAAASUVORK5CYII=";

    private final FlowLayoutEngine engine = new FlowLayoutEngine();

    @Test
    void shouldEmitHeadingAndBlankLinesAndParagraph() {
        List<BodySection> sections = List.of(
                BodySection.fromParagraphs("s1", 1, Optional.of("Introdução"), List.of("Primeiro parágrafo."))
        );

        List<DocxBlock> blocks = engine.render(sections, ctx(false));

        // heading + 1 blank line after + paragraph
        assertEquals(3, blocks.size());
        assertInstanceOf(DocxParagraph.class, blocks.get(0));
        assertInstanceOf(DocxBlankLine.class, blocks.get(1));
        assertInstanceOf(DocxParagraph.class, blocks.get(2));

        DocxParagraph heading = (DocxParagraph) blocks.get(0);
        assertEquals("1 Introdução", heading.runs().get(0).text());
    }

    @Test
    void shouldEmitPageBreakBeforePrimarySection() {
        List<BodySection> sections = List.of(
                BodySection.fromParagraphs("s1", 1, Optional.of("Introdução"), List.of("Parágrafo 1.")),
                BodySection.fromParagraphs("s2", 1, Optional.of("Desenvolvimento"), List.of("Parágrafo 2."))
        );

        List<DocxBlock> blocks = engine.render(sections, ctx(true));

        long pageBreaks = blocks.stream().filter(b -> b instanceof DocxPageBreak).count();
        assertEquals(1, pageBreaks);
    }

    @Test
    void shouldNotEmitPageBreakBeforePrimarySectionWhenDisabled() {
        List<BodySection> sections = List.of(
                BodySection.fromParagraphs("s1", 1, Optional.of("Introdução"), List.of("Parágrafo 1.")),
                BodySection.fromParagraphs("s2", 1, Optional.of("Desenvolvimento"), List.of("Parágrafo 2."))
        );

        List<DocxBlock> blocks = engine.render(sections, ctx(false));

        long pageBreaks = blocks.stream().filter(b -> b instanceof DocxPageBreak).count();
        assertEquals(0, pageBreaks);
    }

    @Test
    void shouldInsertBlankLineBeforeSecondSectionTitleWhenPrecededByParagraph() {
        List<BodySection> sections = List.of(
                BodySection.fromParagraphs("s1", 1, Optional.of("Introdução"), List.of("Parágrafo.")),
                BodySection.fromParagraphs("s2", 2, Optional.of("Subseção"), List.of("Outro parágrafo."))
        );

        List<DocxBlock> blocks = engine.render(sections, ctx(false));

        // heading s1, blank-after, paragraph, blank-before-s2, heading s2, blank-after, paragraph
        long blanks = blocks.stream().filter(b -> b instanceof DocxBlankLine).count();
        assertEquals(3, blanks);
    }

    @Test
    void shouldCollectSectionMetadataInContext() {
        FlowRenderingContext ctx = ctx(false);
        List<BodySection> sections = List.of(
                BodySection.fromParagraphs("intro", 1, Optional.of("Introdução"), List.of("P1.")),
                BodySection.fromParagraphs("sub", 2, Optional.of("Subseção"), List.of("P2."))
        );

        engine.render(sections, ctx);

        assertEquals(2, ctx.sectionMetas().size());
        assertEquals("intro", ctx.sectionMetas().get(0).id());
        assertEquals("sub", ctx.sectionMetas().get(1).id());
    }

    @Test
    void shouldCollectFigureMetadataInContext() {
        BodyFigure figure = new BodyFigure(
                "fig1", Optional.empty(), "Legenda da figura", Optional.empty(),
                new BodyImageSource(ImageSourceType.DATA_URI, ONE_PIXEL_PNG, "Legenda alternativa", null)
        );
        FlowRenderingContext ctx = ctxWithFigure(figure);
        List<BodySection> sections = List.of(
                new BodySection("s1", 1, Optional.of("Intro"), List.of(figure))
        );

        engine.render(sections, ctx);

        assertEquals(1, ctx.figureMetas().size());
        assertEquals("fig1", ctx.figureMetas().get(0).id());
        assertEquals("1", ctx.figureMetas().get(0).number());
    }

    @Test
    void shouldRenderSectionWithoutTitle() {
        List<BodySection> sections = List.of(
                new BodySection("intro", 1, Optional.empty(),
                        List.of(new BodyParagraph(List.of(
                                new com.abntbuilder.formatter.engine.model.content.bodycontent.BodyText(
                                        "Parágrafo.", com.abntbuilder.formatter.engine.model.content.bodycontent.InlineFormatting.none())
                        ))))
        );

        List<DocxBlock> blocks = engine.render(sections, ctx(false));
        // no heading, only paragraph — no blank lines
        assertEquals(1, blocks.size());
        assertInstanceOf(DocxParagraph.class, blocks.get(0));
    }

    // --- Helpers ---

    private FlowRenderingContext ctx(boolean pageBreakBeforePrimary) {
        DocumentProfile profile = profile(pageBreakBeforePrimary);
        BodyContentComponentRule rule = (BodyContentComponentRule) profile.componentRules().get(0);
        StyleResolver styleResolver = new StyleResolver(profile);
        return new FlowRenderingContext(
                Phase0Index.empty(),
                rule,
                styleResolver,
                new DisplayObjectRenderingState<>(List.of()),
                new DisplayObjectRenderingState<>(List.of()),
                new DisplayObjectRenderingState<>(List.of()),
                new DisplayObjectRenderingState<>(List.of()),
                new DisplayObjectRenderingState<>(List.of())
        );
    }

    private FlowRenderingContext ctxWithFigure(BodyFigure figure) {
        DocumentProfile profile = profile(false);
        BodyContentComponentRule rule = (BodyContentComponentRule) profile.componentRules().get(0);
        StyleResolver styleResolver = new StyleResolver(profile);
        return new FlowRenderingContext(
                Phase0Index.empty(),
                rule,
                styleResolver,
                new DisplayObjectRenderingState<>(List.of(figure)),
                new DisplayObjectRenderingState<>(List.of()),
                new DisplayObjectRenderingState<>(List.of()),
                new DisplayObjectRenderingState<>(List.of()),
                new DisplayObjectRenderingState<>(List.of())
        );
    }

    private static DocumentProfile profile(boolean pageBreakBeforePrimary) {
        return new DocumentProfile(
                "test",
                "Test",
                new PageRule(
                        BigDecimal.valueOf(21), BigDecimal.valueOf(29.7),
                        BigDecimal.valueOf(3), BigDecimal.valueOf(2),
                        BigDecimal.valueOf(2), BigDecimal.valueOf(3),
                        PageOrientation.PORTRAIT
                ),
                List.of(
                        style("bodyContent.heading1", StyleType.HEADING_1),
                        style("bodyContent.heading2", StyleType.HEADING_2),
                        style("bodyContent.heading3", StyleType.HEADING_3),
                        style("bodyContent.paragraph", StyleType.PARAGRAPH),
                        style("bodyContent.directShortQuote", StyleType.PARAGRAPH),
                        style("bodyContent.directLongQuote", StyleType.PARAGRAPH),
                        style("bodyContent.indirectCitation", StyleType.PARAGRAPH),
                        style("bodyContent.citationOfCitation", StyleType.PARAGRAPH),
                        style("bodyContent.figure.caption", StyleType.PARAGRAPH),
                        style("bodyContent.figure.source", StyleType.PARAGRAPH),
                        style("bodyContent.table.caption", StyleType.PARAGRAPH),
                        style("bodyContent.table.source", StyleType.PARAGRAPH),
                        style("bodyContent.table.header", StyleType.PARAGRAPH),
                        style("bodyContent.table.cell", StyleType.PARAGRAPH),
                        style("bodyContent.list.ordered", StyleType.PARAGRAPH),
                        style("bodyContent.list.unordered", StyleType.PARAGRAPH),
                        style("bodyContent.frame.caption", StyleType.PARAGRAPH),
                        style("bodyContent.frame.source", StyleType.PARAGRAPH),
                        style("bodyContent.frame.header", StyleType.PARAGRAPH),
                        style("bodyContent.frame.cell", StyleType.PARAGRAPH)
                ),
                List.of(new BodyContentComponentRule(
                        "bodyContent",
                        new BodyContentStyleMapping(
                                List.of("bodyContent.heading1", "bodyContent.heading2", "bodyContent.heading3"),
                                "bodyContent.paragraph",
                                "bodyContent.directShortQuote",
                                "bodyContent.directLongQuote",
                                "bodyContent.indirectCitation",
                                "bodyContent.citationOfCitation",
                                "bodyContent.list.ordered",
                                "bodyContent.list.unordered",
                                "bodyContent.paragraph",
                                "bodyContent.paragraph",
                                "bodyContent.paragraph"
                        ),
                        new BodyContentNumberingRule(true, ".", ""),
                        new BodyContentLayoutRule(1, 1, pageBreakBeforePrimary, "bodyContent.paragraph"),
                        figureRule(),
                        tableRule(),
                        frameRule(),
                        codeListingRule(),
                        chartRule(),
                        new CitationFormattingRule("p. ", "; ", "et al.", " apud ", "[...]", "grifo nosso", "grifo do autor", "informação verbal", ", ", ", ", "(", ")"),
                        new CrossReferenceLabelsRule("Seção", "Figura", "Tabela", "Quadro", "Gráfico", "Listagem", "Equação")
                )),
                List.of("bodyContent")
        );
    }

    private static StyleRule style(String id, StyleType type) {
        return new StyleRule(
                id, type, "Arial", BigDecimal.valueOf(12),
                TextAlignment.JUSTIFIED, BigDecimal.valueOf(1.5),
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, false, false, false
        );
    }

    private static FigureRule figureRule() {
        return new FigureRule(
                "bodyContent.figure.caption", "bodyContent.figure.source",
                "Figura {number} - {caption}", "Fonte: {source}",
                new DisplayObjectContinuationLabels("continua", "continuação", "conclusão"),
                DisplayObjectSourcePlacement.LAST_PART_ONLY,
                TextAlignment.CENTER,
                BigDecimal.valueOf(16), BigDecimal.valueOf(18),
                BigDecimal.valueOf(96), 2_000_000, 10,
                ImageFitPolicy.SCALE_DOWN_PRESERVE_ASPECT_RATIO,
                NumberingStrategy.GLOBAL_SEQUENTIAL, "Figura", null
        );
    }

    private static TableRule tableRule() {
        return new TableRule(
                "bodyContent.table.caption", "bodyContent.table.source",
                "bodyContent.table.header", "bodyContent.table.cell",
                "Tabela {number} - {caption}", "Fonte: {source}",
                new DisplayObjectContinuationLabels("continua", "continuação", "conclusão"),
                DisplayObjectSourcePlacement.LAST_PART_ONLY,
                TextAlignment.CENTER, BigDecimal.valueOf(100), true,
                NumberingStrategy.GLOBAL_SEQUENTIAL, "Tabela", null
        );
    }

    private static FrameRule frameRule() {
        return new FrameRule(
                "bodyContent.frame.caption", "bodyContent.frame.source",
                "bodyContent.frame.header", "bodyContent.frame.cell",
                "Quadro {number} - {caption}", "Fonte: {source}",
                new DisplayObjectContinuationLabels("continua", "continuação", "conclusão"),
                DisplayObjectSourcePlacement.LAST_PART_ONLY,
                TextAlignment.CENTER, BigDecimal.valueOf(100), true,
                NumberingStrategy.GLOBAL_SEQUENTIAL, "Quadro", null
        );
    }

    private static CodeListingRule codeListingRule() {
        return new CodeListingRule(
                "bodyContent.figure.caption", "bodyContent.figure.source",
                "bodyContent.paragraph",
                "Código-fonte {number} - {caption}", "Fonte: {source}",
                new DisplayObjectContinuationLabels("continua", "continuação", "conclusão"),
                DisplayObjectSourcePlacement.LAST_PART_ONLY,
                NumberingStrategy.GLOBAL_SEQUENTIAL, "Código-fonte", null
        );
    }

    private static ChartRule chartRule() {
        return new ChartRule(
                "bodyContent.figure.caption", "bodyContent.figure.source",
                "Gráfico {number} - {caption}", "Fonte: {source}",
                new DisplayObjectContinuationLabels("continua", "continuação", "conclusão"),
                DisplayObjectSourcePlacement.LAST_PART_ONLY,
                figureRule(),
                NumberingStrategy.GLOBAL_SEQUENTIAL, "Gráfico", null
        );
    }
}
