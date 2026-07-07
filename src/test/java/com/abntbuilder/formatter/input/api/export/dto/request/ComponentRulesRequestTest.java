package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.profile.component.ComponentRule;
import com.abntbuilder.formatter.engine.model.profile.TextAlignment;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.BodyContentComponentRule;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.DisplayObjectSourcePlacement;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.ImageFitPolicy;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.NumberingStrategy;
import com.abntbuilder.formatter.engine.model.profile.component.singlepage.SinglePageComponentRule;
import com.abntbuilder.formatter.engine.model.profile.layout.singlepage.HorizontalPlacementStrategy;
import com.abntbuilder.formatter.engine.model.profile.layout.singlepage.SinglePageAnchorStrategy;
import com.abntbuilder.formatter.engine.model.profile.layout.singlepage.SinglePageLineHeightStrategy;
import com.abntbuilder.formatter.engine.model.profile.layout.singlepage.SinglePageSafetyPolicyId;
import com.abntbuilder.formatter.engine.model.profile.layout.singlepage.SpacerStylePolicy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ComponentRulesRequestTest {

    @Test
    void shouldConvertSinglePageComponentRuleWhenPresent() {
        ComponentRulesRequest request = new ComponentRulesRequest(null, titlePageRuleRequest(), null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null);

        List<ComponentRule> rules = request.toDomain();

        assertEquals(1, rules.size());
        SinglePageComponentRule rule = assertInstanceOf(SinglePageComponentRule.class, rules.getFirst());
        assertEquals("titlePage", rule.componentId());
        assertEquals("titlePage.nature", rule.styleMapping().get("nature"));
    }

    @Test
    void shouldReturnEmptyRulesWhenNoComponentRuleIsPresent() {
        ComponentRulesRequest request = new ComponentRulesRequest(null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null);

        assertEquals(List.of(), request.toDomain());
    }

    @Test
    void shouldConvertBodyContentComponentRuleWhenPresent() {
        ComponentRulesRequest request = new ComponentRulesRequest(null, null, null, bodyContentRuleRequest(),
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null);

        List<ComponentRule> rules = request.toDomain();

        assertEquals(1, rules.size());
        BodyContentComponentRule rule = assertInstanceOf(BodyContentComponentRule.class, rules.getFirst());
        assertEquals("bodyContent", rule.componentId());
        assertEquals("bodyContent.heading1", rule.styleMapping().sectionTitleStyleIdForLevel(1));
        assertEquals("bodyContent.paragraph", rule.styleMapping().paragraphStyleId());
        assertEquals("", rule.numbering().primarySuffix());
        assertEquals("bodyContent.table.caption", rule.table().captionStyleId());
    }

    private static SinglePageComponentRuleRequest titlePageRuleRequest() {
        return new SinglePageComponentRuleRequest(
                "titlePage",
                Map.of(
                        "nature", new SlotRuleRequest("COMPOSED_TEXT", true,
                                "{workType} para {degreeObjective}.", List.of("workType", "degreeObjective"),
                                null, null, null, null)
                ),
                Map.of("nature", "titlePage.nature"),
                new SinglePageLayoutRuleRequest(
                        List.of(new SinglePageGroupRuleRequest(
                                "titlePage.natureBlock",
                                true,
                                List.of(new SinglePageItemRuleRequest(
                                        "nature",
                                        true,
                                        null,
                                        HorizontalPlacementStrategy.FROM_PAGE_CENTER_TO_RIGHT_MARGIN,
                                        1
                                ))
                        )),
                        List.of(),
                        singlePageLayoutPolicy()
                )
        );
    }

    private static BodyContentComponentRuleRequest bodyContentRuleRequest() {
        return new BodyContentComponentRuleRequest(
                "bodyContent",
                new BodyContentStyleMappingRequest(
                        List.of("bodyContent.heading1", "bodyContent.heading2", "bodyContent.heading3"),
                        "bodyContent.paragraph",
                        "bodyContent.paragraph",
                        "bodyContent.longQuote",
                        "bodyContent.paragraph",
                        "bodyContent.paragraph",
                        "bodyContent.list.ordered",
                        "bodyContent.list.unordered",
                        "bodyContent.paragraph",
                        "bodyContent.footnoteCall",
                        "bodyContent.footnoteText"
                ),
                new BodyContentNumberingRuleRequest(true, ".", ""),
                new BodyContentLayoutRuleRequest(1, 1, false, "bodyContent.paragraph"),
                figureRuleRequest(),
                tableRuleRequest(),
                frameRuleRequest(),
                new CodeListingRuleRequest(
                        "bodyContent.codeListing.caption",
                        "bodyContent.codeListing.source",
                        "codeStyle",
                        "Código {number} - {caption}",
                        "Fonte: {source}",
                        new DisplayObjectContinuationLabelsRequest("continua", "continuação", "conclusão"),
                        DisplayObjectSourcePlacement.LAST_PART_ONLY,
                        NumberingStrategy.GLOBAL_SEQUENTIAL,
                        "Código-fonte",
                        null
                ),
                new ChartRuleRequest(
                        "bodyContent.chart.caption",
                        "bodyContent.chart.source",
                        "Gráfico {number} - {caption}",
                        "Fonte: {source}",
                        new DisplayObjectContinuationLabelsRequest("continua", "continuação", "conclusão"),
                        DisplayObjectSourcePlacement.LAST_PART_ONLY,
                        figureRuleRequest(),
                        NumberingStrategy.GLOBAL_SEQUENTIAL,
                        "Gráfico",
                        null
                ),
                new CitationFormattingRuleRequest("p. ", "; ", "et al.", " apud ", "[...]", "grifo nosso", "grifo do autor", "informação verbal", ", ", ", ", "(", ")", null, null, null, null, null, null, null),
                new CrossReferenceLabelsRuleRequest("Seção", "Figura", "Tabela", "Quadro", "Gráfico", "Listagem", "Equação")
        );
    }

    private static FigureRuleRequest figureRuleRequest() {
        return new FigureRuleRequest(
                "bodyContent.figure.caption",
                "bodyContent.figure.source",
                "Figura {number} - {caption}",
                "Fonte: {source}",
                new DisplayObjectContinuationLabelsRequest("continua", "continuação", "conclusão"),
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

    private static TableRuleRequest tableRuleRequest() {
        return new TableRuleRequest(
                "bodyContent.table.caption",
                "bodyContent.table.source",
                "bodyContent.table.header",
                "bodyContent.table.cell",
                "Tabela {number} - {caption}",
                "Fonte: {source}",
                new DisplayObjectContinuationLabelsRequest("continua", "continuação", "conclusão"),
                DisplayObjectSourcePlacement.LAST_PART_ONLY,
                TextAlignment.CENTER,
                BigDecimal.valueOf(100),
                true,
                NumberingStrategy.GLOBAL_SEQUENTIAL,
                "Tabela",
                null
        );
    }

    private static FrameRuleRequest frameRuleRequest() {
        return new FrameRuleRequest(
                "bodyContent.frame.caption",
                "bodyContent.frame.source",
                "bodyContent.frame.header",
                "bodyContent.frame.cell",
                "Quadro {number} - {caption}",
                "Fonte: {source}",
                new DisplayObjectContinuationLabelsRequest("continua", "continuação", "conclusão"),
                DisplayObjectSourcePlacement.LAST_PART_ONLY,
                TextAlignment.CENTER,
                BigDecimal.valueOf(100),
                true,
                NumberingStrategy.GLOBAL_SEQUENTIAL,
                "Quadro",
                null
        );
    }

    private static SinglePageLayoutPolicyRequest singlePageLayoutPolicy() {
        return new SinglePageLayoutPolicyRequest(
                SinglePageAnchorStrategy.LAST_GROUP_AT_SAFE_AREA_END,
                SinglePageLineHeightStrategy.MAX_EXACT_LINE_HEIGHT,
                SpacerStylePolicy.NEXT_GROUP_STYLE,
                SinglePageSafetyPolicyId.MARGIN_BASED
        );
    }
}
