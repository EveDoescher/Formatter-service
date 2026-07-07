package com.abntbuilder.formatter.rendering.bodycontent;

import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyAbbreviation;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyContentComponent;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyCrossReference;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyFigure;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyImageSource;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyParagraph;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodySection;
import com.abntbuilder.formatter.engine.model.content.bodycontent.CrossReferenceDisplayMode;
import com.abntbuilder.formatter.engine.model.content.bodycontent.CrossReferenceTargetType;
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
import com.abntbuilder.formatter.engine.model.output.DocxParagraph;
import com.abntbuilder.formatter.engine.model.output.DocxRun;
import com.abntbuilder.formatter.rendering.phase0.DisplayObjectCollector;
import com.abntbuilder.formatter.rendering.phase0.Phase0Index;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class BodyContentRendererMetadataTest {

    private static final String ONE_PIXEL_PNG =
            "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/p9sAAAAASUVORK5CYII=";

    private final BodyContentRenderer renderer = new BodyContentRenderer("bodyContent");
    private final DisplayObjectCollector collector = new DisplayObjectCollector();

    @Test
    void shouldEmitSectionMetadata() {
        BodySection s1 = new BodySection("sec-intro", 1, Optional.of("Introdução"), List.of());
        BodySection s2 = new BodySection("sec-dev", 1, Optional.of("Desenvolvimento"), List.of());
        BodyContentComponent component = new BodyContentComponent("bodyContent", List.of(s1, s2));

        BodyContentRenderResult result = renderer.renderWithMetadata(component, profile());

        assertThat(result.metadata().sections()).hasSize(2);
        assertThat(result.metadata().sections().get(0).id()).isEqualTo("sec-intro");
        assertThat(result.metadata().sections().get(0).level()).isEqualTo(1);
        assertThat(result.metadata().sections().get(0).renderedTitle()).startsWith("1");
        assertThat(result.metadata().sections().get(0).renderedNumber()).isEqualTo("1");
        assertThat(result.metadata().sections().get(1).renderedTitle()).startsWith("2");
        assertThat(result.metadata().sections().get(1).renderedNumber()).isEqualTo("2");
    }

    @Test
    void shouldEmitFigureMetadata() {
        BodyFigure figure = new BodyFigure(
                "fig-1",
                Optional.empty(),
                "Diagrama de componentes",
                Optional.empty(),
                new BodyImageSource(ImageSourceType.DATA_URI, ONE_PIXEL_PNG, "Diagrama")
        );
        BodySection section = new BodySection("sec-1", 1, Optional.of("Seção"), List.of(figure));
        BodyContentComponent component = new BodyContentComponent("bodyContent", List.of(section));

        BodyContentRenderResult result = renderer.renderWithMetadata(component, profile());

        assertThat(result.metadata().figures()).hasSize(1);
        assertThat(result.metadata().figures().get(0).id()).isEqualTo("fig-1");
        assertThat(result.metadata().figures().get(0).number()).isEqualTo("1");
        assertThat(result.metadata().figures().get(0).caption()).isEqualTo("Diagrama de componentes");
    }

    @Test
    void shouldEmitAbbreviationMetadataOnlyOnce() {
        BodyAbbreviation abbr = new BodyAbbreviation("ABNT", "Associação Brasileira de Normas Técnicas");
        BodyParagraph p1 = new BodyParagraph(List.of(abbr));
        BodyParagraph p2 = new BodyParagraph(List.of(abbr));
        BodySection section = new BodySection("sec-1", 1, Optional.of("Seção"), List.of(p1, p2));
        BodyContentComponent component = new BodyContentComponent("bodyContent", List.of(section));

        BodyContentRenderResult result = renderer.renderWithMetadata(component, profile());

        assertThat(result.metadata().abbreviations()).hasSize(1);
        assertThat(result.metadata().abbreviations().get(0).abbreviation()).isEqualTo("ABNT");
        assertThat(result.metadata().abbreviations().get(0).expansion())
                .isEqualTo("Associação Brasileira de Normas Técnicas");
    }

    @Test
    void shouldResolveCrossReferenceToFigureLabelAndNumber() {
        BodyFigure figure = new BodyFigure(
                "fig-arch",
                Optional.empty(),
                "Arquitetura do sistema",
                Optional.empty(),
                new BodyImageSource(ImageSourceType.DATA_URI, ONE_PIXEL_PNG, "Arquitetura")
        );
        BodyCrossReference ref = new BodyCrossReference("fig-arch", CrossReferenceTargetType.FIGURE, CrossReferenceDisplayMode.LABEL_AND_NUMBER);
        BodyParagraph paragraph = new BodyParagraph(List.of(ref));
        BodySection section = new BodySection("sec-1", 1, Optional.of("Seção"), List.of(figure, paragraph));
        BodyContentComponent component = new BodyContentComponent("bodyContent", List.of(section));
        DocumentProfile prof = profile();
        Phase0Index phase0Index = collector.collect(List.of(component), prof);

        BodyContentRenderResult result = renderer.renderWithPhase0(component, prof, phase0Index);

        boolean hasResolvedReference = result.blocks().stream()
                .filter(b -> b instanceof DocxParagraph)
                .map(b -> (DocxParagraph) b)
                .anyMatch(para -> para.runs().stream().map(DocxRun::text).reduce("", String::concat).equals("Figura 1"));
        assertThat(hasResolvedReference).as("Expected a paragraph with exactly 'Figura 1' (resolved cross-reference)").isTrue();
    }

    @Test
    void shouldResolveCrossReferenceToSectionNumber() {
        BodySection target = new BodySection("sec-target", 1, Optional.of("Metodologia"), List.of());
        BodyCrossReference ref = new BodyCrossReference("sec-target", CrossReferenceTargetType.SECTION, CrossReferenceDisplayMode.NUMBER_ONLY);
        BodySection refSection = new BodySection("sec-ref", 1, Optional.of("Introdução"), List.of(new BodyParagraph(List.of(ref))));
        BodyContentComponent component = new BodyContentComponent("bodyContent", List.of(target, refSection));
        DocumentProfile prof = profile();
        Phase0Index phase0Index = collector.collect(List.of(component), prof);

        BodyContentRenderResult result = renderer.renderWithPhase0(component, prof, phase0Index);

        DocxParagraph docxParagraph = result.blocks().stream()
                .filter(b -> b instanceof DocxParagraph para && para.runs().stream().anyMatch(r -> r.text().equals("1")))
                .map(b -> (DocxParagraph) b)
                .findFirst()
                .orElseThrow(() -> new AssertionError("No paragraph with section number '1' found"));
        assertThat(docxParagraph.runs()).anyMatch(r -> r.text().equals("1"));
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
                        style("bodyContent.frame.cell", false, false),
                        style("bodyContent.footnoteCall", false, false),
                        style("bodyContent.footnoteText", false, false)
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
                                "bodyContent.footnoteCall",
                                "bodyContent.footnoteText"
                        ),
                        new BodyContentNumberingRule(true, ".", ""),
                        new BodyContentLayoutRule(1, 1, false, "bodyContent.paragraph"),
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

    private static CodeListingRule codeListingRule() {
        return new CodeListingRule(
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

    private static ChartRule chartRule() {
        return new ChartRule(
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
