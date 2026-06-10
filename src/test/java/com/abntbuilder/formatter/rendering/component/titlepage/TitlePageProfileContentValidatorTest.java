package com.abntbuilder.formatter.rendering.component.titlepage;

import com.abntbuilder.formatter.document.component.titlepage.TitlePageComponent;
import com.abntbuilder.formatter.document.component.titlepage.TitlePageNature;
import com.abntbuilder.formatter.profile.model.component.titlepage.TitlePageComponentRule;
import com.abntbuilder.formatter.profile.model.component.titlepage.TitlePageStyleMapping;
import com.abntbuilder.formatter.profile.model.component.titlepage.TitlePageTextTemplateRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.LayoutGapRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageGroupRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageItemRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageLayoutPolicy;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageLayoutRule;
import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;
import com.abntbuilder.formatter.shared.exception.InvalidTitlePageContentException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TitlePageProfileContentValidatorTest {

    private final TitlePageProfileContentValidator validator = new TitlePageProfileContentValidator();

    @Test
    void shouldFailWhenRequiredItemHasNoContent() {
        InvalidTitlePageContentException exception = assertThrows(
                InvalidTitlePageContentException.class,
                () -> validator.validate(validTitlePage(), ruleWithRequiredAdvisor())
        );

        assertEquals("titlePage required item has no content: advisor.", exception.getMessage());
    }

    @Test
    void shouldFailWhenRequiredGroupHasNoContent() {
        InvalidTitlePageContentException exception = assertThrows(
                InvalidTitlePageContentException.class,
                () -> validator.validate(validTitlePage(), ruleWithRequiredOptionalOnlyGroup())
        );

        assertEquals("titlePage required group has no content: titlePage.optional.", exception.getMessage());
    }

    @Test
    void shouldFailWhenProfileDeclaresUnknownTitlePageItem() {
        InvalidProfileStructureException exception = assertThrows(
                InvalidProfileStructureException.class,
                () -> validator.validate(validTitlePage(), ruleWithUnknownItem())
        );

        assertEquals("Unknown titlePage item id: unknownItem", exception.getMessage());
    }

    @Test
    void shouldFailWhenComponentIdIsNotTitlePage() {
        InvalidProfileStructureException exception = assertThrows(
                InvalidProfileStructureException.class,
                () -> validator.validate(validTitlePage(), ruleWithComponentId("cover"))
        );

        assertEquals("titlePage componentId must be titlePage.", exception.getMessage());
    }

    private static TitlePageComponent validTitlePage() {
        return new TitlePageComponent(
                List.of("Autor"),
                "Titulo",
                Optional.empty(),
                new TitlePageNature(
                        "Trabalho academico",
                        "avaliacao parcial",
                        "Curso",
                        "Universidade"
                ),
                Optional.empty(),
                Optional.empty(),
                "Limeira",
                "2026"
        );
    }

    private static TitlePageComponentRule ruleWithRequiredAdvisor() {
        return ruleWithGroups(List.of(group("titlePage.advisor", true, item("advisor", true))));
    }

    private static TitlePageComponentRule ruleWithRequiredOptionalOnlyGroup() {
        return ruleWithGroups(List.of(group("titlePage.optional", true, item("coadvisor", false))));
    }

    private static TitlePageComponentRule ruleWithUnknownItem() {
        return ruleWithGroups(List.of(group("titlePage.unknown", true, item("unknownItem", true))));
    }

    private static TitlePageComponentRule ruleWithComponentId(String componentId) {
        return new TitlePageComponentRule(
                componentId,
                new com.abntbuilder.formatter.profile.model.component.ComponentContentBindings(java.util.Map.of()),
                styleMapping(),
                textTemplates(),
                layoutRule(List.of(group("titlePage.authors", true, item("authors", true))))
        );
    }

    private static TitlePageComponentRule ruleWithGroups(List<SinglePageGroupRule> groups) {
        return new TitlePageComponentRule(
                "titlePage",
                new com.abntbuilder.formatter.profile.model.component.ComponentContentBindings(java.util.Map.of()),
                styleMapping(),
                textTemplates(),
                layoutRule(groups)
        );
    }

    private static SinglePageLayoutRule layoutRule(List<SinglePageGroupRule> groups) {
        return new SinglePageLayoutRule(
                groups,
                groups.size() <= 1 ? List.of() : List.of(new LayoutGapRule(
                        groups.get(0).id(),
                        groups.get(1).id(),
                        BigDecimal.ONE
                )),
                SinglePageLayoutPolicy.defaultSinglePagePolicy()
        );
    }

    private static SinglePageGroupRule group(String groupId, boolean required, SinglePageItemRule item) {
        return new SinglePageGroupRule(
                groupId,
                required,
                List.of(item)
        );
    }

    private static SinglePageItemRule item(String itemId, boolean required) {
        return new SinglePageItemRule(itemId, required, Optional.empty());
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
}
