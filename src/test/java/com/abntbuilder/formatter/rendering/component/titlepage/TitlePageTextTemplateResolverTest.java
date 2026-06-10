package com.abntbuilder.formatter.rendering.component.titlepage;

import com.abntbuilder.formatter.document.component.titlepage.AcademicPerson;
import com.abntbuilder.formatter.document.component.titlepage.TitlePageNature;
import com.abntbuilder.formatter.profile.model.component.titlepage.TitlePageTextTemplateRule;
import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TitlePageTextTemplateResolverTest {

    private final TitlePageTextTemplateResolver resolver = new TitlePageTextTemplateResolver();

    @Test
    void shouldResolveNatureTemplateFromProfile() {
        String text = resolver.resolveNature(templates(), nature());

        assertEquals(
                "Trabalho de conclusao de curso para obtencao do titulo de graduacao em Analise e Desenvolvimento de Sistemas apresentado a Universidade Paulista.",
                text
        );
    }

    @Test
    void shouldResolveAdvisorTemplateWithoutDoubleSpacesWhenAcademicTitleIsAbsent() {
        String text = resolver.resolveAdvisor(
                templates(),
                new AcademicPerson("Pessoa Orientadora Teste", Optional.empty())
        );

        assertEquals("Orientador(a): Pessoa Orientadora Teste.", text);
    }

    @Test
    void shouldRejectUnknownPlaceholder() {
        TitlePageTextTemplateRule templates = new TitlePageTextTemplateRule(
                "{workType} {unknownPlaceholder}",
                "Orientador(a): {academicTitle} {name}.",
                "Coorientador(a): {academicTitle} {name}."
        );

        InvalidProfileStructureException exception = assertThrows(
                InvalidProfileStructureException.class,
                () -> resolver.resolveNature(templates, nature())
        );

        assertEquals("Unknown titlePage template placeholder: unknownPlaceholder", exception.getMessage());
    }

    private static TitlePageTextTemplateRule templates() {
        return new TitlePageTextTemplateRule(
                "{workType} para {degreeObjective} em {courseName} apresentado a {institutionName}.",
                "Orientador(a): {academicTitle} {name}.",
                "Coorientador(a): {academicTitle} {name}."
        );
    }

    private static TitlePageNature nature() {
        return new TitlePageNature(
                "Trabalho de conclusao de curso",
                "obtencao do titulo de graduacao",
                "Analise e Desenvolvimento de Sistemas",
                "Universidade Paulista"
        );
    }
}
