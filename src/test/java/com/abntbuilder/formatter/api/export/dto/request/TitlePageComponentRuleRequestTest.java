package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.titlepage.TitlePageComponentRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.HorizontalPlacementStrategy;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageAnchorStrategy;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageLineHeightStrategy;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageSafetyPolicyId;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SpacerStylePolicy;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TitlePageComponentRuleRequestTest {

    @Test
    void shouldConvertTitlePageComponentRuleRequestToDomain() {
        TitlePageComponentRuleRequest request = new TitlePageComponentRuleRequest(
                "titlePage",
                Map.of("title", "work.title"),
                styleMapping(),
                textTemplates(),
                new SinglePageLayoutRuleRequest(
                        List.of(
                                new SinglePageGroupRuleRequest(
                                        "titlePage.natureBlock",
                                        true,
                                        List.of(new SinglePageItemRuleRequest(
                                                "nature",
                                                true,
                                                null,
                                                HorizontalPlacementStrategy.FROM_PAGE_CENTER_TO_RIGHT_MARGIN,
                                                1
                                        ))
                                )
                        ),
                        List.of(),
                        singlePageLayoutPolicy()
                )
        );

        TitlePageComponentRule rule = request.toDomain();

        assertEquals("titlePage", rule.componentId());
        assertEquals("work.title", rule.contentBindings().sourceFor("title").orElseThrow());
        assertEquals("titlePage.nature", rule.styleMapping().styleIdForItem("nature"));
        assertEquals(
                "{workType} para {degreeObjective} em {courseName} apresentado a {institutionName}.",
                rule.textTemplates().natureTemplate()
        );
        assertEquals("titlePage.natureBlock", rule.layoutRule().groups().getFirst().id());
        assertEquals(
                HorizontalPlacementStrategy.FROM_PAGE_CENTER_TO_RIGHT_MARGIN,
                rule.layoutRule().groups().getFirst().items().getFirst().horizontalPlacement().strategy()
        );
        assertEquals(1, rule.layoutRule().groups().getFirst().items().getFirst().blankLinesAfter());
    }

    private static TitlePageStyleMappingRequest styleMapping() {
        return new TitlePageStyleMappingRequest(
                "titlePage.author",
                "titlePage.title",
                "titlePage.subtitle",
                "titlePage.nature",
                "titlePage.advisor",
                "titlePage.coadvisor",
                "titlePage.bottom",
                "titlePage.bottom"
        );
    }

    private static TitlePageTextTemplateRuleRequest textTemplates() {
        return new TitlePageTextTemplateRuleRequest(
                "{workType} para {degreeObjective} em {courseName} apresentado a {institutionName}.",
                "Orientador(a): {academicTitle} {name}.",
                "Coorientador(a): {academicTitle} {name}."
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
