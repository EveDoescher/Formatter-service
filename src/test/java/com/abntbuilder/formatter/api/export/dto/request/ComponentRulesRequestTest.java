package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.ComponentRule;
import com.abntbuilder.formatter.profile.model.component.bodycontent.BodyContentComponentRule;
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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ComponentRulesRequestTest {

    @Test
    void shouldConvertTitlePageComponentRuleWhenPresent() {
        ComponentRulesRequest request = new ComponentRulesRequest(null, titlePageRuleRequest(), null, null);

        List<ComponentRule> rules = request.toDomain();

        assertEquals(1, rules.size());
        TitlePageComponentRule rule = assertInstanceOf(TitlePageComponentRule.class, rules.getFirst());
        assertEquals("titlePage", rule.componentId());
        assertEquals("titlePage.nature", rule.styleMapping().styleIdForItem("nature"));
    }

    @Test
    void shouldReturnEmptyRulesWhenNoComponentRuleIsPresent() {
        ComponentRulesRequest request = new ComponentRulesRequest(null, null, null, null);

        assertEquals(List.of(), request.toDomain());
    }

    @Test
    void shouldConvertBodyContentComponentRuleWhenPresent() {
        ComponentRulesRequest request = new ComponentRulesRequest(null, null, null, bodyContentRuleRequest());

        List<ComponentRule> rules = request.toDomain();

        assertEquals(1, rules.size());
        BodyContentComponentRule rule = assertInstanceOf(BodyContentComponentRule.class, rules.getFirst());
        assertEquals("bodyContent", rule.componentId());
        assertEquals("bodyContent.heading1", rule.styleMapping().sectionTitleStyleIdForLevel(1));
        assertEquals("bodyContent.paragraph", rule.styleMapping().paragraphStyleId());
        assertEquals("", rule.numbering().primarySuffix());
    }

    private static TitlePageComponentRuleRequest titlePageRuleRequest() {
        return new TitlePageComponentRuleRequest(
                "titlePage",
                Map.of("title", "work.title"),
                new TitlePageStyleMappingRequest(
                        "titlePage.author",
                        "titlePage.title",
                        "titlePage.subtitle",
                        "titlePage.nature",
                        "titlePage.advisor",
                        "titlePage.coadvisor",
                        "titlePage.bottom",
                        "titlePage.bottom"
                ),
                new TitlePageTextTemplateRuleRequest(
                        "{workType} para {degreeObjective} em {courseName} apresentado a {institutionName}.",
                        "Orientador(a): {academicTitle} {name}.",
                        "Coorientador(a): {academicTitle} {name}."
                ),
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
                        "bodyContent.paragraph"
                ),
                new BodyContentNumberingRuleRequest(true, ".", ""),
                new BodyContentLayoutRuleRequest(1, 1, false, "bodyContent.paragraph")
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
