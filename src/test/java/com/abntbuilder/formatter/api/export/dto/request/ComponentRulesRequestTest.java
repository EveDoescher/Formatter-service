package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.ComponentRule;
import com.abntbuilder.formatter.profile.model.component.titlepage.TitlePageComponentRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.HorizontalPlacementStrategy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ComponentRulesRequestTest {

    @Test
    void shouldConvertTitlePageComponentRuleWhenPresent() {
        ComponentRulesRequest request = new ComponentRulesRequest(null, titlePageRuleRequest());

        List<ComponentRule> rules = request.toDomain();

        assertEquals(1, rules.size());
        TitlePageComponentRule rule = assertInstanceOf(TitlePageComponentRule.class, rules.getFirst());
        assertEquals("titlePage", rule.componentId());
        assertEquals("titlePage.nature", rule.styleMapping().styleIdForItem("nature"));
    }

    @Test
    void shouldReturnEmptyRulesWhenNoComponentRuleIsPresent() {
        ComponentRulesRequest request = new ComponentRulesRequest(null, null);

        assertEquals(List.of(), request.toDomain());
    }

    private static TitlePageComponentRuleRequest titlePageRuleRequest() {
        return new TitlePageComponentRuleRequest(
                "titlePage",
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
                        null
                )
        );
    }
}
