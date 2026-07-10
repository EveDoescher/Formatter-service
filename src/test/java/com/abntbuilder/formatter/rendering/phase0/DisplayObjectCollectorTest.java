package com.abntbuilder.formatter.rendering.phase0;

import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyAbbreviation;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyContentComponent;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyFigure;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyImageSource;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyParagraph;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodySection;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyTable;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyTableCell;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyTableColumn;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyTableRow;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyText;
import com.abntbuilder.formatter.engine.model.content.bodycontent.ImageSourceType;
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
import com.abntbuilder.formatter.engine.model.profile.component.elementindex.ElementType;
import com.abntbuilder.formatter.rendering.bodycontent.BodyDisplayObjectMetadata;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DisplayObjectCollectorTest {

    private static final String ONE_PIXEL_PNG =
            "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/p9sAAAAASUVORK5CYII=";

    private final DisplayObjectCollector collector = new DisplayObjectCollector();

    @Test
    void shouldIndexFiguresInGlobalSequentialOrder() {
        BodyFigure fig1 = figure("fig-1", "Diagrama de arquitetura");
        BodyFigure fig2 = figure("fig-2", "Fluxo de dados");
        BodySection section = new BodySection("sec-1", 1, Optional.of("Capítulo 1"), List.of(fig1, fig2));
        BodyContentComponent component = new BodyContentComponent("bodyContent", List.of(section));

        Phase0Index index = collector.collect(List.of(component), profile());

        assertThat(index.elements(ElementType.FIGURE)).hasSize(2);
        assertThat(index.elements(ElementType.FIGURE).get("fig-1").number()).isEqualTo("1");
        assertThat(index.elements(ElementType.FIGURE).get("fig-2").number()).isEqualTo("2");
    }

    @Test
    void shouldIndexTablesWithCorrectCaptions() {
        BodyTable table = table("tbl-1", "Comparação de resultados");
        BodySection section = new BodySection("sec-1", 1, Optional.of("Resultados"), List.of(table));
        BodyContentComponent component = new BodyContentComponent("bodyContent", List.of(section));

        Phase0Index index = collector.collect(List.of(component), profile());

        assertThat(index.elements(ElementType.TABLE)).hasSize(1);
        BodyDisplayObjectMetadata meta = index.elements(ElementType.TABLE).get("tbl-1");
        assertThat(meta.number()).isEqualTo("1");
        assertThat(meta.caption()).isEqualTo("Comparação de resultados");
    }

    @Test
    void shouldIndexSectionsWithNumberedTitles() {
        BodySection s1 = new BodySection("sec-intro", 1, Optional.of("Introdução"), List.of());
        BodySection s2 = new BodySection("sec-dev", 1, Optional.of("Desenvolvimento"), List.of());
        BodyContentComponent component = new BodyContentComponent("bodyContent", List.of(s1, s2));

        Phase0Index index = collector.collect(List.of(component), profile());

        assertThat(index.sections()).hasSize(2);
        assertThat(index.sections().get("sec-intro").renderedNumber()).isEqualTo("1");
        assertThat(index.sections().get("sec-dev").renderedNumber()).isEqualTo("2");
        assertThat(index.sections().get("sec-intro").renderedTitle()).startsWith("1");
        assertThat(index.sections().get("sec-dev").renderedTitle()).startsWith("2");
    }

    @Test
    void shouldReturnEmptyIndexWhenNoBodyContentPresent() {
        Phase0Index index = collector.collect(List.of(), profile());

        assertThat(index.elements(ElementType.FIGURE)).isEmpty();
        assertThat(index.elements(ElementType.TABLE)).isEmpty();
        assertThat(index.sections()).isEmpty();
    }

    @Test
    void shouldNotIndexContinuationPartsOfMultiPageFigure() {
        BodyFigure figPart1 = new BodyFigure("fig-multi-1", Optional.of("fig-multi"), "Diagrama longo",
                Optional.of("Autoria própria"),
                new BodyImageSource(ImageSourceType.DATA_URI, ONE_PIXEL_PNG, "Parte 1"));
        BodyFigure figPart2 = new BodyFigure("fig-multi-2", Optional.of("fig-multi"), "Diagrama longo",
                Optional.of("Autoria própria"),
                new BodyImageSource(ImageSourceType.DATA_URI, ONE_PIXEL_PNG, "Parte 2"));
        BodySection section = new BodySection("sec-1", 1, Optional.of("Seção"), List.of(figPart1, figPart2));
        BodyContentComponent component = new BodyContentComponent("bodyContent", List.of(section));

        Phase0Index index = collector.collect(List.of(component), profile());

        assertThat(index.elements(ElementType.FIGURE)).hasSize(1);
        assertThat(index.elements(ElementType.FIGURE).values().iterator().next().number()).isEqualTo("1");
    }

    @Test
    void shouldCollectAbbreviationsFromParagraphInlines() {
        BodyParagraph paragraph = new BodyParagraph(List.of(
                new BodyText("A sigla "),
                new BodyAbbreviation("ABNT", "Associação Brasileira de Normas Técnicas"),
                new BodyText(" e também "),
                new BodyAbbreviation("NBR", "Norma Brasileira")
        ));
        BodySection section = new BodySection("sec-1", 1, Optional.of("Introdução"), List.of(paragraph));
        BodyContentComponent component = new BodyContentComponent("bodyContent", List.of(section));

        Phase0Index index = collector.collect(List.of(component), profile());

        assertThat(index.abbreviations()).hasSize(2);
        assertThat(index.abbreviations()).extracting("abbreviation")
                .containsExactly("ABNT", "NBR");
    }

    @Test
    void shouldNotDuplicateAbbreviationsAppearedMultipleTimes() {
        BodyParagraph para1 = new BodyParagraph(List.of(
                new BodyAbbreviation("ABNT", "Associação Brasileira de Normas Técnicas")));
        BodyParagraph para2 = new BodyParagraph(List.of(
                new BodyAbbreviation("ABNT", "Associação Brasileira de Normas Técnicas")));
        BodySection section = new BodySection("sec-1", 1, Optional.of("Seção"), List.of(para1, para2));
        BodyContentComponent component = new BodyContentComponent("bodyContent", List.of(section));

        Phase0Index index = collector.collect(List.of(component), profile());

        assertThat(index.abbreviations()).hasSize(1);
    }

    private static BodyFigure figure(String id, String caption) {
        return new BodyFigure(id, Optional.empty(), caption, Optional.empty(),
                new BodyImageSource(ImageSourceType.DATA_URI, ONE_PIXEL_PNG, caption));
    }

    private static BodyTable table(String id, String caption) {
        BodyTableRow row = new BodyTableRow(List.of(
                new BodyTableCell("Valor A"), new BodyTableCell("Valor B")));
        return new BodyTable(id, Optional.empty(), caption, Optional.empty(),
                List.of(new BodyTableColumn("Col A"), new BodyTableColumn("Col B")),
                List.of(row));
    }

    private static DocumentProfile profile() {
        return new DocumentProfile(
                "test-profile",
                "Test Profile",
                pageRule(),
                List.of(
                        style("bodyContent.heading1", StyleType.HEADING_1, true, true),
                        style("bodyContent.paragraph", false, false),
                        style("bodyContent.figure.caption", false, false),
                        style("bodyContent.figure.source", false, false),
                        style("bodyContent.table.caption", false, false),
                        style("bodyContent.table.source", false, false),
                        style("bodyContent.table.header", true, false),
                        style("bodyContent.table.cell", false, false),
                        style("bodyContent.frame.caption", false, false),
                        style("bodyContent.frame.source", false, false),
                        style("bodyContent.frame.header", true, false),
                        style("bodyContent.frame.cell", false, false),
                        style("bodyContent.directShortQuote", false, false),
                        style("bodyContent.directLongQuote", false, false),
                        style("bodyContent.indirectCitation", false, false),
                        style("bodyContent.citationOfCitation", false, false),
                        style("bodyContent.list.ordered", false, false),
                        style("bodyContent.list.unordered", false, false),
                        style("bodyContent.footnoteCall", false, false),
                        style("bodyContent.footnoteText", false, false)
                ),
                List.of(new BodyContentComponentRule(
                        "bodyContent",
                        new BodyContentStyleMapping(
                                List.of("bodyContent.heading1"),
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

    private static PageRule pageRule() {
        return new PageRule(BigDecimal.valueOf(21), BigDecimal.valueOf(29.7),
                BigDecimal.valueOf(3), BigDecimal.valueOf(2), BigDecimal.valueOf(2),
                BigDecimal.valueOf(3), PageOrientation.PORTRAIT);
    }

    private static StyleRule style(String id, boolean bold, boolean uppercase) {
        return style(id, StyleType.PARAGRAPH, bold, uppercase);
    }

    private static StyleRule style(String id, StyleType type, boolean bold, boolean uppercase) {
        return new StyleRule(id, type, "Times New Roman", BigDecimal.valueOf(12),
                TextAlignment.JUSTIFIED, BigDecimal.valueOf(1.5),
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO,
                bold, false, uppercase);
    }

    private static FigureRule figureRule() {
        return new FigureRule(
                "bodyContent.figure.caption", "bodyContent.figure.source",
                "Figura {number} - {caption}", "Fonte: {source}",
                new DisplayObjectContinuationLabels("continua", "continuação", "conclusão"),
                DisplayObjectSourcePlacement.LAST_PART_ONLY, TextAlignment.CENTER,
                BigDecimal.valueOf(16), BigDecimal.valueOf(18), BigDecimal.valueOf(96),
                2_000_000, 10, ImageFitPolicy.SCALE_DOWN_PRESERVE_ASPECT_RATIO,
                NumberingStrategy.GLOBAL_SEQUENTIAL, "Figura", null);
    }

    private static TableRule tableRule() {
        return new TableRule(
                "bodyContent.table.caption", "bodyContent.table.source",
                "bodyContent.table.header", "bodyContent.table.cell",
                "Tabela {number} - {caption}", "Fonte: {source}",
                new DisplayObjectContinuationLabels("continua", "continuação", "conclusão"),
                DisplayObjectSourcePlacement.LAST_PART_ONLY, TextAlignment.CENTER,
                BigDecimal.valueOf(100), true,
                NumberingStrategy.GLOBAL_SEQUENTIAL, "Tabela", null);
    }

    private static FrameRule frameRule() {
        return new FrameRule(
                "bodyContent.frame.caption", "bodyContent.frame.source",
                "bodyContent.frame.header", "bodyContent.frame.cell",
                "Quadro {number} - {caption}", "Fonte: {source}",
                new DisplayObjectContinuationLabels("continua", "continuação", "conclusão"),
                DisplayObjectSourcePlacement.LAST_PART_ONLY, TextAlignment.CENTER,
                BigDecimal.valueOf(100), true,
                NumberingStrategy.GLOBAL_SEQUENTIAL, "Quadro", null);
    }

    private static CodeListingRule codeListingRule() {
        return new CodeListingRule(
                "bodyContent.figure.caption", "bodyContent.figure.source",
                "bodyContent.paragraph",
                "Código-fonte {number} - {caption}", "Fonte: {source}",
                new DisplayObjectContinuationLabels("continua", "continuação", "conclusão"),
                DisplayObjectSourcePlacement.LAST_PART_ONLY,
                NumberingStrategy.GLOBAL_SEQUENTIAL, "Código-fonte", null);
    }

    private static ChartRule chartRule() {
        return new ChartRule(
                "bodyContent.figure.caption", "bodyContent.figure.source",
                "Gráfico {number} - {caption}", "Fonte: {source}",
                new DisplayObjectContinuationLabels("continua", "continuação", "conclusão"),
                DisplayObjectSourcePlacement.LAST_PART_ONLY,
                figureRule(),
                NumberingStrategy.GLOBAL_SEQUENTIAL, "Gráfico", null);
    }
}
