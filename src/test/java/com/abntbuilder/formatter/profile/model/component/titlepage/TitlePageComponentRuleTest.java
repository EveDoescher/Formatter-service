package com.abntbuilder.formatter.profile.model.component.titlepage;

import com.abntbuilder.formatter.profile.model.layout.singlepage.LayoutGapRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageGroupRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageItemRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageLayoutPolicy;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageLayoutRule;
import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TitlePageComponentRuleTest {

    @Test
    void shouldCreateValidRule() {
        TitlePageComponentRule rule = new TitlePageComponentRule(
                "titlePage",
                new com.abntbuilder.formatter.profile.model.component.ComponentContentBindings(java.util.Map.of()),
                styleMapping(),
                textTemplates(),
                layoutRule()
        );

        assertEquals("titlePage", rule.componentId());
        assertEquals("titlePage.nature", rule.styleMapping().styleIdForItem("nature"));
    }

    @Test
    void shouldRejectBlankStyleMapping() {
        InvalidProfileStructureException exception = assertThrows(
                InvalidProfileStructureException.class,
                () -> new TitlePageStyleMapping(
                        " ",
                        "titlePage.title",
                        "titlePage.subtitle",
                        "titlePage.nature",
                        "titlePage.advisor",
                        "titlePage.coadvisor",
                        "titlePage.bottom",
                        "titlePage.bottom"
                )
        );

        assertEquals("authorsStyleId must not be blank.", exception.getMessage());
    }

    @Test
    void shouldRejectBlankNatureTemplate() {
        InvalidProfileStructureException exception = assertThrows(
                InvalidProfileStructureException.class,
                () -> new TitlePageTextTemplateRule(
                        " ",
                        "Orientador(a): {academicTitle} {name}.",
                        "Coorientador(a): {academicTitle} {name}."
                )
        );

        assertEquals("natureTemplate must not be blank.", exception.getMessage());
    }

    private static TitlePageStyleMapping styleMapping() {
        return new TitlePageStyleMapping(
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

    private static TitlePageTextTemplateRule textTemplates() {
        return new TitlePageTextTemplateRule(
                "{workType} para {degreeObjective} em {courseName} apresentado a {institutionName}.",
                "Orientador(a): {academicTitle} {name}.",
                "Coorientador(a): {academicTitle} {name}."
        );
    }

    private static SinglePageLayoutRule layoutRule() {
        return new SinglePageLayoutRule(
                List.of(
                        group("titlePage.authors", "authors"),
                        group("titlePage.titleBlock", "title"),
                        group("titlePage.natureBlock", "nature"),
                        group("titlePage.bottom", "city")
                ),
                List.of(
                        gap("titlePage.authors", "titlePage.titleBlock", 20),
                        gap("titlePage.titleBlock", "titlePage.natureBlock", 35),
                        gap("titlePage.natureBlock", "titlePage.bottom", 45)
                ),
                SinglePageLayoutPolicy.defaultSinglePagePolicy()
        );
    }

    private static SinglePageGroupRule group(String groupId, String itemId) {
        return new SinglePageGroupRule(
                groupId,
                true,
                List.of(new SinglePageItemRule(itemId, true, Optional.empty()))
        );
    }

    private static LayoutGapRule gap(String fromGroupId, String toGroupId, int weight) {
        return new LayoutGapRule(fromGroupId, toGroupId, BigDecimal.valueOf(weight));
    }
}
