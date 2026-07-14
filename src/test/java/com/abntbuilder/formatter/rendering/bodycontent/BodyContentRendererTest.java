package com.abntbuilder.formatter.rendering.bodycontent;

import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyContentComponent;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyCitationMode;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyCitationType;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyFigure;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyImageSource;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyParagraph;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodySection;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyTable;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyTableCell;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyTableColumn;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyTableRow;
import com.abntbuilder.formatter.engine.model.content.bodycontent.CitationAuthor;
import com.abntbuilder.formatter.engine.model.content.bodycontent.CitationSource;
import com.abntbuilder.formatter.engine.model.content.bodycontent.ImageSourceType;
import com.abntbuilder.formatter.engine.model.output.DocxBlankLine;
import com.abntbuilder.formatter.engine.model.output.DocxBlock;
import com.abntbuilder.formatter.engine.model.output.DocxImageBlock;
import com.abntbuilder.formatter.engine.model.output.DocxPageBreak;
import com.abntbuilder.formatter.engine.model.output.DocxParagraph;
import com.abntbuilder.formatter.engine.model.output.DocxTableBlock;
import com.abntbuilder.formatter.engine.model.profile.DocumentProfile;
import com.abntbuilder.formatter.engine.model.profile.PageOrientation;
import com.abntbuilder.formatter.engine.model.profile.PageRule;
import com.abntbuilder.formatter.engine.model.profile.StyleRule;
import com.abntbuilder.formatter.engine.model.profile.StyleType;
import com.abntbuilder.formatter.engine.model.profile.TextAlignment;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.BodyContentComponentRule;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.CitationFormattingRule;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.CrossReferenceLabelsRule;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.BodyContentLayoutRule;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.BodyContentNumberingRule;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.BodyContentStyleMapping;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.DisplayObjectContinuationLabels;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.DisplayObjectSourcePlacement;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.FigureRule;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.FrameRule;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.ImageFitPolicy;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.NumberingStrategy;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.TableRule;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BodyContentRendererTest {

    private static final String ONE_PIXEL_PNG_DATA_URI =
            "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/p9sAAAAASUVORK5CYII=";

    private final BodyContentRenderer renderer = new BodyContentRenderer("bodyContent");

    @Test
    void shouldRenderSectionTitlesAndParagraphsWithProfileStyles() {
        BodyContentComponent component = new BodyContentComponent("bodyContent", List.of(
                BodySection.fromParagraphs(
                        "introducao",
                        1,
                        Optional.of("Introducao"),
                        List.of("Primeiro paragrafo.", "Segundo paragrafo.")
                ),
                BodySection.fromParagraphs(
                        "fundamentacao",
                        2,
                        Optional.of("Fundamentacao"),
                        List.of("Paragrafo de fundamentacao.")
                ),
                BodySection.fromParagraphs(
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
                        "1 Introducao",
                        "Primeiro paragrafo.",
                        "Segundo paragrafo.",
                        "1.1 Fundamentacao",
                        "Paragrafo de fundamentacao.",
                        "1.1.1 Detalhe",
                        "Paragrafo de detalhe."
                ),
                paragraphs.stream().map(p -> p.runs().get(0).text()).toList()
        );
        assertEquals("bodyContent.heading1", paragraphs.get(0).styleRule().id());
        assertEquals("bodyContent.paragraph", paragraphs.get(1).styleRule().id());
        assertEquals("bodyContent.heading2", paragraphs.get(3).styleRule().id());
        assertEquals("bodyContent.heading3", paragraphs.get(5).styleRule().id());
    }

    @Test
    void shouldRenderSectionWithoutTitle() {
        BodyContentComponent component = new BodyContentComponent("bodyContent", List.of(
                BodySection.fromParagraphs("sem-titulo", 1, Optional.empty(), List.of("Paragrafo sem titulo."))
        ));

        List<DocxBlock> blocks = renderer.render(component, profile());

        assertEquals(1, blocks.size());
        assertEquals("Paragrafo sem titulo.", ((DocxParagraph) blocks.getFirst()).runs().get(0).text());
    }

    @Test
    void shouldRenderProfileDrivenBlankLinesAroundSectionTitles() {
        BodyContentComponent component = new BodyContentComponent("bodyContent", List.of(
                BodySection.fromParagraphs("introducao", 1, Optional.of("Introducao"), List.of("Primeiro paragrafo.")),
                BodySection.fromParagraphs("desenvolvimento", 1, Optional.of("Desenvolvimento"), List.of("Segundo paragrafo."))
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
        BodyContentComponent component = new BodyContentComponent("bodyContent", List.of(
                BodySection.fromParagraphs("introducao", 1, Optional.of("Introducao"), List.of("Primeiro paragrafo.")),
                BodySection.fromParagraphs("desenvolvimento", 1, Optional.of("Desenvolvimento"), List.of("Segundo paragrafo."))
        ));

        List<DocxBlock> blocks = renderer.render(component, profileWithPrimarySectionPageBreak());

        assertEquals(DocxPageBreak.class, blocks.get(3).getClass());
        assertEquals("2 Desenvolvimento", ((DocxParagraph) blocks.get(4)).runs().get(0).text());
    }

    @Test
    void shouldRenderLongQuoteBlockWithProfileMappedStyle() {
        BodyContentComponent component = new BodyContentComponent("bodyContent", List.of(
                new BodySection(
                        "citacoes",
                        1,
                        Optional.of("Citacoes"),
                        List.of(
                                new BodyParagraph("Paragrafo comum."),
                                new com.abntbuilder.formatter.engine.model.content.bodycontent.BodyLongQuote(
                                        "Citacao direta longa com mais de tres linhas.",
                                        BodyCitationMode.PARENTHETICAL,
                                        Optional.of(source("SOBRENOME TESTE UM", "2020", "11")),
                                        Optional.empty(),
                                        Optional.empty()
                                )
                        )
                )
        ));

        List<DocxParagraph> paragraphs = renderer.render(component, profile())
                .stream()
                .filter(DocxParagraph.class::isInstance)
                .map(DocxParagraph.class::cast)
                .toList();

        assertEquals("bodyContent.paragraph", paragraphs.get(1).styleRule().id());
        assertEquals("bodyContent.directLongQuote", paragraphs.get(2).styleRule().id());
    }

    @Test
    void shouldRenderFigureContinuationAsSingleNumberedDisplayObject() {
        BodyContentComponent component = new BodyContentComponent("bodyContent", List.of(
                new BodySection(
                        "figuras",
                        1,
                        Optional.of("Figuras"),
                        List.of(
                                figure("figura-arquitetura-parte-1", "grupo-arquitetura", Optional.empty()),
                                figure("figura-arquitetura-parte-2", "grupo-arquitetura", Optional.of("Elaboração teste"))
                        )
                )
        ));

        List<DocxBlock> blocks = renderer.render(component, profile());
        List<DocxParagraph> paragraphs = blocks.stream()
                .filter(DocxParagraph.class::isInstance)
                .map(DocxParagraph.class::cast)
                .toList();
        List<DocxImageBlock> images = blocks.stream()
                .filter(DocxImageBlock.class::isInstance)
                .map(DocxImageBlock.class::cast)
                .toList();

        assertEquals("Figura 1 - Arquitetura de teste (continua)", paragraphs.get(1).runs().get(0).text());
        assertEquals("Figura 1 - Arquitetura de teste (conclusão)", paragraphs.get(2).runs().get(0).text());
        assertEquals("Fonte: Elaboração teste", paragraphs.get(3).runs().get(0).text());
        assertEquals(2, images.size());
        assertEquals(true, paragraphs.get(1).keepWithNext());
        assertEquals(true, images.getFirst().keepLines());
    }

    @Test
    void shouldRenderTableContinuationAsSingleNumberedDisplayObject() {
        BodyContentComponent component = new BodyContentComponent("bodyContent", List.of(
                new BodySection(
                        "tabelas",
                        1,
                        Optional.of("Tabelas"),
                        List.of(
                                table("tabela-resultados-parte-1", "grupo-resultados", Optional.empty()),
                                table("tabela-resultados-parte-2", "grupo-resultados", Optional.of("Elaboração teste"))
                        )
                )
        ));

        List<DocxBlock> blocks = renderer.render(component, profile());
        List<DocxParagraph> paragraphs = blocks.stream()
                .filter(DocxParagraph.class::isInstance)
                .map(DocxParagraph.class::cast)
                .toList();
        List<DocxTableBlock> tables = blocks.stream()
                .filter(DocxTableBlock.class::isInstance)
                .map(DocxTableBlock.class::cast)
                .toList();

        assertEquals("Tabela 1 - Resultados de teste (continua)", paragraphs.get(1).runs().get(0).text());
        assertEquals("Tabela 1 - Resultados de teste (conclusão)", paragraphs.get(2).runs().get(0).text());
        assertEquals("Fonte: Elaboração teste", paragraphs.get(3).runs().get(0).text());
        assertEquals(2, tables.size());
        assertEquals(List.of("Cenário", "Resultado"), tables.getFirst().headers());
        assertEquals(true, tables.getFirst().repeatHeaderOnPageBreak());
        assertEquals(true, paragraphs.get(1).keepWithNext());
    }

    @Test
    void shouldEmitEmphasisSuffixRunForQuoteTextWithEmphasisOursMarker() {
        BodyContentComponent component = new BodyContentComponent("bodyContent", List.of(
                new BodySection(
                        "sec",
                        1,
                        Optional.empty(),
                        List.of(new com.abntbuilder.formatter.engine.model.content.bodycontent.BodyParagraph(
                                List.of(new com.abntbuilder.formatter.engine.model.content.bodycontent.BodyQuoteText(
                                        com.abntbuilder.formatter.engine.model.content.bodycontent.BodyQuoteType.SHORT,
                                        "texto citado",
                                        com.abntbuilder.formatter.engine.model.content.bodycontent.InlineFormatting.none(),
                                        List.of(com.abntbuilder.formatter.engine.model.content.bodycontent.BodyQuoteMarker.emphasisOurs())
                                ))
                        ))
                )
        ));

        List<DocxParagraph> paragraphs = renderer.render(component, profile())
                .stream()
                .filter(DocxParagraph.class::isInstance)
                .map(DocxParagraph.class::cast)
                .toList();

        DocxParagraph paragraph = paragraphs.getFirst();
        assertEquals(2, paragraph.runs().size());
        assertEquals("\"texto citado\"", paragraph.runs().get(0).text());
        assertEquals(" (grifo nosso)", paragraph.runs().get(1).text());
    }

    @Test
    void shouldEmitEmphasisSuffixRunForQuoteTextWithEmphasisAuthorMarker() {
        BodyContentComponent component = new BodyContentComponent("bodyContent", List.of(
                new BodySection(
                        "sec",
                        1,
                        Optional.empty(),
                        List.of(new com.abntbuilder.formatter.engine.model.content.bodycontent.BodyParagraph(
                                List.of(new com.abntbuilder.formatter.engine.model.content.bodycontent.BodyQuoteText(
                                        com.abntbuilder.formatter.engine.model.content.bodycontent.BodyQuoteType.SHORT,
                                        "texto citado",
                                        com.abntbuilder.formatter.engine.model.content.bodycontent.InlineFormatting.none(),
                                        List.of(com.abntbuilder.formatter.engine.model.content.bodycontent.BodyQuoteMarker.emphasisAuthor())
                                ))
                        ))
                )
        ));

        List<DocxParagraph> paragraphs = renderer.render(component, profile())
                .stream()
                .filter(DocxParagraph.class::isInstance)
                .map(DocxParagraph.class::cast)
                .toList();

        DocxParagraph paragraph = paragraphs.getFirst();
        assertEquals(2, paragraph.runs().size());
        assertEquals("\"texto citado\"", paragraph.runs().get(0).text());
        assertEquals(" (grifo do autor)", paragraph.runs().get(1).text());
    }

    private static CitationSource source(String author, String year, String page) {
        return new CitationSource(
                List.of(CitationAuthor.person(toDisplayName(author))),
                year,
                page == null ? Optional.empty() : Optional.of(page)
        );
    }

    private static BodyFigure figure(String id, String continuationGroupId, Optional<String> source) {
        return new BodyFigure(
                id,
                Optional.of(continuationGroupId),
                "Arquitetura de teste",
                source,
                new BodyImageSource(ImageSourceType.DATA_URI, ONE_PIXEL_PNG_DATA_URI, "Imagem teste")
        );
    }

    private static BodyTable table(String id, String continuationGroupId, Optional<String> source) {
        return new BodyTable(
                id,
                Optional.of(continuationGroupId),
                "Resultados de teste",
                source,
                List.of(
                        new BodyTableColumn("Cenário"),
                        new BodyTableColumn("Resultado")
                ),
                List.of(
                        new BodyTableRow(List.of(new BodyTableCell("Teste A"), new BodyTableCell("Aprovado"))),
                        new BodyTableRow(List.of(new BodyTableCell("Teste B"), new BodyTableCell("Aprovado")))
                )
        );
    }

    private static String toDisplayName(String author) {
        return switch (author) {
            case "SOBRENOME TESTE UM" -> "Sobrenome Teste Um";
            case "SOBRENOME TESTE DOIS" -> "Sobrenome Teste Dois";
            default -> author;
        };
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
                        style("bodyContent.paragraph", false, false),
                        style("bodyContent.directShortQuote", false, false),
                        style("bodyContent.directLongQuote", false, false),
                        style("bodyContent.indirectCitation", false, false),
                        style("bodyContent.citationOfCitation", false, false),
                        style("bodyContent.figure.caption", false, false),
                        style("bodyContent.figure.source", false, false),
                        style("bodyContent.table.caption", false, false),
                        style("bodyContent.table.source", false, false),
                        style("bodyContent.table.header", true, false),
                        style("bodyContent.table.cell", false, false),
                        style("bodyContent.list.ordered", false, false),
                        style("bodyContent.list.unordered", false, false),
                        style("bodyContent.frame.caption", false, false),
                        style("bodyContent.frame.source", false, false),
                        style("bodyContent.frame.header", true, false),
                        style("bodyContent.frame.cell", false, false)
                ),
                List.of(new BodyContentComponentRule(
                        "bodyContent",
                        true,
                        null,
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
                                "bodyContent.footnoteCall",
                                "bodyContent.footnoteText"
                        ),
                        new BodyContentNumberingRule(true, ".", ""),
                        new BodyContentLayoutRule(1, 1, false, false, "bodyContent.paragraph", null),
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

    private static DocumentProfile profileWithPrimarySectionPageBreak() {
        return new DocumentProfile(
                "test-profile",
                "Test Profile",
                pageRule(),
                List.of(
                        style("bodyContent.heading1", StyleType.HEADING_1, true, true),
                        style("bodyContent.heading2", StyleType.HEADING_2, true, false),
                        style("bodyContent.heading3", StyleType.HEADING_3, true, false),
                        style("bodyContent.paragraph", false, false),
                        style("bodyContent.directShortQuote", false, false),
                        style("bodyContent.directLongQuote", false, false),
                        style("bodyContent.indirectCitation", false, false),
                        style("bodyContent.citationOfCitation", false, false),
                        style("bodyContent.figure.caption", false, false),
                        style("bodyContent.figure.source", false, false),
                        style("bodyContent.table.caption", false, false),
                        style("bodyContent.table.source", false, false),
                        style("bodyContent.table.header", true, false),
                        style("bodyContent.table.cell", false, false),
                        style("bodyContent.list.ordered", false, false),
                        style("bodyContent.list.unordered", false, false),
                        style("bodyContent.frame.caption", false, false),
                        style("bodyContent.frame.source", false, false),
                        style("bodyContent.frame.header", true, false),
                        style("bodyContent.frame.cell", false, false)
                ),
                List.of(new BodyContentComponentRule(
                        "bodyContent",
                        true,
                        null,
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
                                "bodyContent.footnoteCall",
                                "bodyContent.footnoteText"
                        ),
                        new BodyContentNumberingRule(true, ".", ""),
                        new BodyContentLayoutRule(1, 1, true, false, "bodyContent.paragraph", null),
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

    private static FigureRule figureRule() {
        return new FigureRule(
                "bodyContent.figure.caption",
                "bodyContent.figure.source",
                "Figura {number} - {caption}",
                "Fonte: {source}",
                new DisplayObjectContinuationLabels("continua", "continuação", "conclusão"),
                DisplayObjectSourcePlacement.LAST_PART_ONLY,
                TextAlignment.CENTER,
                BigDecimal.valueOf(16),
                BigDecimal.valueOf(18),
                BigDecimal.valueOf(96),
                2_000_000,
                10,
                ImageFitPolicy.SCALE_DOWN_PRESERVE_ASPECT_RATIO,
                NumberingStrategy.GLOBAL_SEQUENTIAL,
                "Figura",
                null
        );
    }

    private static TableRule tableRule() {
        return new TableRule(
                "bodyContent.table.caption",
                "bodyContent.table.source",
                "bodyContent.table.header",
                "bodyContent.table.cell",
                "Tabela {number} - {caption}",
                "Fonte: {source}",
                new DisplayObjectContinuationLabels("continua", "continuação", "conclusão"),
                DisplayObjectSourcePlacement.LAST_PART_ONLY,
                TextAlignment.CENTER,
                BigDecimal.valueOf(100),
                true,
                NumberingStrategy.GLOBAL_SEQUENTIAL,
                "Tabela",
                null
        );
    }

    private static FrameRule frameRule() {
        return new FrameRule(
                "bodyContent.frame.caption",
                "bodyContent.frame.source",
                "bodyContent.frame.header",
                "bodyContent.frame.cell",
                "Quadro {number} - {caption}",
                "Fonte: {source}",
                new DisplayObjectContinuationLabels("continua", "continuação", "conclusão"),
                DisplayObjectSourcePlacement.LAST_PART_ONLY,
                TextAlignment.CENTER,
                BigDecimal.valueOf(100),
                true,
                NumberingStrategy.GLOBAL_SEQUENTIAL,
                "Quadro",
                null
        );
    }

    private static com.abntbuilder.formatter.engine.model.profile.component.bodycontent.CodeListingRule codeListingRule() {
        return new com.abntbuilder.formatter.engine.model.profile.component.bodycontent.CodeListingRule(
                "bodyContent.figure.caption",
                "bodyContent.figure.source",
                "bodyContent.paragraph",
                "Código-fonte {number} - {caption}",
                "Fonte: {source}",
                new DisplayObjectContinuationLabels("continua", "continuação", "conclusão"),
                DisplayObjectSourcePlacement.LAST_PART_ONLY,
                NumberingStrategy.GLOBAL_SEQUENTIAL,
                "Código-fonte",
                null
        );
    }

    private static com.abntbuilder.formatter.engine.model.profile.component.bodycontent.ChartRule chartRule() {
        return new com.abntbuilder.formatter.engine.model.profile.component.bodycontent.ChartRule(
                "bodyContent.figure.caption",
                "bodyContent.figure.source",
                "Gráfico {number} - {caption}",
                "Fonte: {source}",
                new DisplayObjectContinuationLabels("continua", "continuação", "conclusão"),
                DisplayObjectSourcePlacement.LAST_PART_ONLY,
                figureRule(),
                NumberingStrategy.GLOBAL_SEQUENTIAL,
                "Gráfico",
                null
        );
    }
}
