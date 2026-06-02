package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.titlepage.TitlePageComponentRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.HorizontalPlacementStrategy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TitlePageComponentRuleRequestTest {

    @Test
    void shouldConvertTitlePageComponentRuleRequestToDomain() {
        TitlePageComponentRuleRequest request = new TitlePageComponentRuleRequest(
                "titlePage",
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
                                                HorizontalPlacementStrategy.FROM_PAGE_CENTER_TO_RIGHT_MARGIN
                                        ))
                                )
                        ),
                        List.of(),
                        null
                )
        );

        TitlePageComponentRule rule = request.toDomain();

        assertEquals("titlePage", rule.componentId());
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
}
