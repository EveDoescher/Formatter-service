package com.abntbuilder.formatter.rendering.bodycontent;

import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyAbbreviation;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyAbbreviation;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyCrossReference;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyFootnote;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyInline;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyText;
import com.abntbuilder.formatter.engine.model.content.bodycontent.CrossReferenceDisplayMode;
import com.abntbuilder.formatter.engine.model.content.bodycontent.CrossReferenceTargetType;
import com.abntbuilder.formatter.engine.model.content.bodycontent.InlineFormatting;
import com.abntbuilder.formatter.engine.model.output.DocxFootnoteContent;
import com.abntbuilder.formatter.engine.model.output.DocxRun;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RunProcessorTest {

    @Test
    void shouldProcessBodyText() {
        BodyInline inline = new BodyText("Texto simples.", InlineFormatting.none());
        List<DocxRun> runs = process(inline);

        assertEquals(1, runs.size());
        assertEquals("Texto simples.", runs.get(0).text());
    }

    @Test
    void shouldProcessBodyAbbreviationAndAccumulateMetadata() {
        FlowRenderingContext ctx = ctx();
        BodyInline inline = new BodyAbbreviation("ABNT", "Associação Brasileira de Normas Técnicas");
        StyleRule baseStyle = baseStyle();
        List<DocxFootnoteContent> footnotes = new ArrayList<>();

        RunProcessor.processAll(List.of(inline), baseStyle, ctx, footnotes);

        assertEquals(1, ctx.abbreviationMetas().size());
        assertEquals("ABNT", ctx.abbreviationMetas().get(0).abbreviation());
        assertEquals("Associação Brasileira de Normas Técnicas", ctx.abbreviationMetas().get(0).expansion());
    }

    @Test
    void shouldNotDuplicateAbbreviationMetadata() {
        FlowRenderingContext ctx = ctx();
        BodyInline abbr = new BodyAbbreviation("ABNT", "Associação Brasileira de Normas Técnicas");
        StyleRule baseStyle = baseStyle();
        List<DocxFootnoteContent> footnotes = new ArrayList<>();

        RunProcessor.processAll(List.of(abbr, abbr), baseStyle, ctx, footnotes);

        assertEquals(1, ctx.abbreviationMetas().size());
    }

    @Test
    void shouldProcessBodyTextWithBoldFormatting() {
        InlineFormatting bold = new InlineFormatting(
                Optional.of(true), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
        BodyInline inline = new BodyText("Texto em negrito.", bold);
        List<DocxRun> runs = process(inline);

        assertEquals(1, runs.size());
        assertEquals("Texto em negrito.", runs.get(0).text());
    }

    @Test
    void shouldProcessFootnoteAndIncrementCounter() {
        FlowRenderingContext ctx = ctx();
        BodyInline footnote = new BodyFootnote(List.of(new BodyText("Nota de rodapé.", InlineFormatting.none())));
        StyleRule baseStyle = baseStyle();
        List<DocxFootnoteContent> footnotes = new ArrayList<>();

        RunProcessor.processAll(List.of(footnote), baseStyle, ctx, footnotes);

        assertEquals(1, footnotes.size());
        assertEquals(1, footnotes.get(0).id());
    }

    @Test
    void shouldResolveCrossReferenceFromPhase0Index() {
        // build Phase0Index with a known section mapping
        var sectionMeta = new com.abntbuilder.formatter.rendering.bodycontent.BodySectionMetadata(
                "sec1", 1, "1 Introdução", "1"
        );
        Phase0Index phase0 = new Phase0Index(
                Map.of("sec1", sectionMeta),
                Map.of(),
                List.of()
        );
        FlowRenderingContext ctx = ctx(phase0);

        BodyInline ref = new BodyCrossReference("sec1", CrossReferenceTargetType.SECTION, CrossReferenceDisplayMode.NUMBER_ONLY);
        StyleRule baseStyle = baseStyle();
        List<DocxFootnoteContent> footnotes = new ArrayList<>();

        List<DocxRun> runs = RunProcessor.processAll(List.of(ref), baseStyle, ctx, footnotes);

        assertEquals(1, runs.size());
        assertEquals("1", runs.get(0).text());
    }

    // --- Helpers ---

    private static List<DocxRun> process(BodyInline inline) {
        FlowRenderingContext ctx = ctx();
        return RunProcessor.process(inline, baseStyle(), ctx, new ArrayList<>());
    }

    private static FlowRenderingContext ctx() {
        return ctx(Phase0Index.empty());
    }

    private static FlowRenderingContext ctx(Phase0Index phase0Index) {
        DocumentProfile profile = profile();
        BodyContentComponentRule rule = (BodyContentComponentRule) profile.componentRules().get(0);
        StyleResolver styleResolver = new StyleResolver(profile);
        return new FlowRenderingContext(
                phase0Index, rule, styleResolver,
                new DisplayObjectRenderingState<>(List.of()),
                new DisplayObjectRenderingState<>(List.of()),
                new DisplayObjectRenderingState<>(List.of()),
                new DisplayObjectRenderingState<>(List.of()),
                new DisplayObjectRenderingState<>(List.of())
        );
    }

    private static StyleRule baseStyle() {
        return new StyleRule(
                "base", StyleType.PARAGRAPH, "Arial", BigDecimal.valueOf(12),
                TextAlignment.JUSTIFIED, BigDecimal.valueOf(1.5),
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, false, false, false
        );
    }

    private static DocumentProfile profile() {
        return new DocumentProfile(
                "test", "Test",
                new PageRule(
                        BigDecimal.valueOf(21), BigDecimal.valueOf(29.7),
                        BigDecimal.valueOf(3), BigDecimal.valueOf(2),
                        BigDecimal.valueOf(2), BigDecimal.valueOf(3),
                        PageOrientation.PORTRAIT
                ),
                List.of(
                        baseStyle(),
                        new StyleRule("bodyContent.indirectCitation", StyleType.PARAGRAPH, "Arial", BigDecimal.valueOf(12),
                                TextAlignment.JUSTIFIED, BigDecimal.valueOf(1.5),
                                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                                BigDecimal.ZERO, BigDecimal.ZERO, false, false, false),
                        new StyleRule("bodyContent.citationOfCitation", StyleType.PARAGRAPH, "Arial", BigDecimal.valueOf(12),
                                TextAlignment.JUSTIFIED, BigDecimal.valueOf(1.5),
                                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                                BigDecimal.ZERO, BigDecimal.ZERO, false, false, false),
                        new StyleRule("bodyContent.directShortQuote", StyleType.PARAGRAPH, "Arial", BigDecimal.valueOf(12),
                                TextAlignment.JUSTIFIED, BigDecimal.valueOf(1.5),
                                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                                BigDecimal.ZERO, BigDecimal.ZERO, false, false, false),
                        new StyleRule("bodyContent.directLongQuote", StyleType.PARAGRAPH, "Arial", BigDecimal.valueOf(12),
                                TextAlignment.JUSTIFIED, BigDecimal.valueOf(1.5),
                                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                                BigDecimal.ZERO, BigDecimal.ZERO, false, false, false),
                        new StyleRule("bodyContent.footnoteCall", StyleType.PARAGRAPH, "Arial", BigDecimal.valueOf(10),
                                TextAlignment.JUSTIFIED, BigDecimal.valueOf(1.5),
                                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                                BigDecimal.ZERO, BigDecimal.ZERO, false, false, false),
                        new StyleRule("bodyContent.footnoteText", StyleType.PARAGRAPH, "Arial", BigDecimal.valueOf(10),
                                TextAlignment.JUSTIFIED, BigDecimal.valueOf(1.5),
                                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                                BigDecimal.ZERO, BigDecimal.ZERO, false, false, false)
                ),
                List.of(new BodyContentComponentRule(
                        "bodyContent",
                        new BodyContentStyleMapping(
                                List.of("base"),
                                "base",
                                "bodyContent.directShortQuote",
                                "bodyContent.directLongQuote",
                                "bodyContent.indirectCitation",
                                "bodyContent.citationOfCitation",
                                "base", "base", "base",
                                "bodyContent.footnoteCall",
                                "bodyContent.footnoteText"
                        ),
                        new BodyContentNumberingRule(true, ".", ""),
                        new BodyContentLayoutRule(1, 1, false, "base"),
                        figureRule(), tableRule(), frameRule(), codeListingRule(), chartRule(),
                        new CitationFormattingRule("p. ", "; ", "et al.", " apud ", "[...]", "grifo nosso", "grifo do autor", "informação verbal", ", ", ", ", "(", ")"),
                        new CrossReferenceLabelsRule("Seção", "Figura", "Tabela", "Quadro", "Gráfico", "Listagem", "Equação")
                )),
                List.of("bodyContent")
        );
    }

    private static FigureRule figureRule() {
        return new FigureRule(
                "base", "base", "Figura {number} - {caption}", "Fonte: {source}",
                new DisplayObjectContinuationLabels("continua", "continuação", "conclusão"),
                DisplayObjectSourcePlacement.LAST_PART_ONLY, TextAlignment.CENTER,
                BigDecimal.valueOf(16), BigDecimal.valueOf(18), BigDecimal.valueOf(96),
                2_000_000, 10, ImageFitPolicy.SCALE_DOWN_PRESERVE_ASPECT_RATIO,
                NumberingStrategy.GLOBAL_SEQUENTIAL, "Figura", null
        );
    }

    private static TableRule tableRule() {
        return new TableRule(
                "base", "base", "base", "base",
                "Tabela {number} - {caption}", "Fonte: {source}",
                new DisplayObjectContinuationLabels("continua", "continuação", "conclusão"),
                DisplayObjectSourcePlacement.LAST_PART_ONLY, TextAlignment.CENTER,
                BigDecimal.valueOf(100), true,
                NumberingStrategy.GLOBAL_SEQUENTIAL, "Tabela", null
        );
    }

    private static FrameRule frameRule() {
        return new FrameRule(
                "base", "base", "base", "base",
                "Quadro {number} - {caption}", "Fonte: {source}",
                new DisplayObjectContinuationLabels("continua", "continuação", "conclusão"),
                DisplayObjectSourcePlacement.LAST_PART_ONLY, TextAlignment.CENTER,
                BigDecimal.valueOf(100), true,
                NumberingStrategy.GLOBAL_SEQUENTIAL, "Quadro", null
        );
    }

    private static CodeListingRule codeListingRule() {
        return new CodeListingRule(
                "base", "base", "base",
                "Código-fonte {number} - {caption}", "Fonte: {source}",
                new DisplayObjectContinuationLabels("continua", "continuação", "conclusão"),
                DisplayObjectSourcePlacement.LAST_PART_ONLY,
                NumberingStrategy.GLOBAL_SEQUENTIAL, "Código-fonte", null
        );
    }

    private static ChartRule chartRule() {
        return new ChartRule(
                "base", "base",
                "Gráfico {number} - {caption}", "Fonte: {source}",
                new DisplayObjectContinuationLabels("continua", "continuação", "conclusão"),
                DisplayObjectSourcePlacement.LAST_PART_ONLY, figureRule(),
                NumberingStrategy.GLOBAL_SEQUENTIAL, "Gráfico", null
        );
    }
}
