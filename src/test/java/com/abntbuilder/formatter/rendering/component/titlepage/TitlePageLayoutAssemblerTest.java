package com.abntbuilder.formatter.rendering.component.titlepage;

import com.abntbuilder.formatter.document.component.titlepage.AcademicPerson;
import com.abntbuilder.formatter.document.component.titlepage.TitlePageComponent;
import com.abntbuilder.formatter.document.component.titlepage.TitlePageNature;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.component.titlepage.TitlePageComponentRule;
import com.abntbuilder.formatter.profile.resolution.ComponentRuleResolver;
import com.abntbuilder.formatter.profile.resolution.ClasspathJsonProfileProvider;
import com.abntbuilder.formatter.rendering.layout.singlepage.HorizontalPlacementResolver;
import com.abntbuilder.formatter.rendering.layout.singlepage.OrderedLayoutGapResolver;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutGroup;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutInput;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutItem;
import com.abntbuilder.formatter.rendering.layout.text.ConservativeTextMeasurer;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TitlePageLayoutAssemblerTest {

    private final DocumentProfile profile = new ClasspathJsonProfileProvider().findById("abnt-unip-profile");
    private final TitlePageComponentRule rule = new ComponentRuleResolver(profile)
            .resolve("titlePage", TitlePageComponentRule.class);
    private final TitlePageLayoutAssembler assembler = new TitlePageLayoutAssembler(
            new ConservativeTextMeasurer(),
            new OrderedLayoutGapResolver(),
            new TitlePageProfileContentValidator(),
            new TitlePageTextTemplateResolver(),
            new HorizontalPlacementResolver()
    );

    @Test
    void shouldAssembleSemanticTitlePageUsingProfileGroupsAndTemplates() {
        SinglePageLayoutInput input = assembler.assemble(component(), profile, rule);

        assertEquals(
                List.of(
                        "titlePage.authors",
                        "titlePage.titleBlock",
                        "titlePage.natureBlock",
                        "titlePage.bottom"
                ),
                input.groups().stream().map(SinglePageLayoutGroup::id).toList()
        );
        assertEquals(3, input.gaps().size());

        SinglePageLayoutItem nature = item(input, "nature");
        SinglePageLayoutItem advisor = item(input, "advisor");

        assertTrue(String.join(" ", nature.visualLines()).contains("Trabalho de conclusao de curso"));
        assertTrue(String.join(" ", advisor.visualLines()).contains("Orientador(a): Prof. Dr. Pessoa Orientadora Teste."));
        assertTrue(nature.measurementArea().orElseThrow().leftIndentCm().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(nature.layoutOverride().leftIndentCm().isPresent());
        assertEquals(1, nature.blankLinesAfter());
    }

    @Test
    void shouldOmitOptionalCoadvisorWithoutCreatingEmptyItem() {
        SinglePageLayoutInput input = assembler.assemble(component(), profile, rule);

        assertFalse(input.groups()
                .stream()
                .flatMap(group -> group.items().stream())
                .map(SinglePageLayoutItem::id)
                .anyMatch("coadvisor"::equals));
    }

    @Test
    void shouldNotAddNatureBlankLineWhenAdvisorAndCoadvisorAreAbsent() {
        SinglePageLayoutInput input = assembler.assemble(componentWithoutAdvisor(), profile, rule);

        assertEquals(0, item(input, "nature").blankLinesAfter());
    }

    @Test
    void shouldAddAdvisorBlankLineOnlyWhenCoadvisorIsPresent() {
        SinglePageLayoutInput inputWithoutCoadvisor = assembler.assemble(component(), profile, rule);
        SinglePageLayoutInput inputWithCoadvisor = assembler.assemble(componentWithCoadvisor(), profile, rule);

        assertEquals(0, item(inputWithoutCoadvisor, "advisor").blankLinesAfter());
        assertEquals(1, item(inputWithCoadvisor, "advisor").blankLinesAfter());
    }

    private static SinglePageLayoutItem item(SinglePageLayoutInput input, String itemId) {
        return input.groups()
                .stream()
                .flatMap(group -> group.items().stream())
                .filter(item -> item.id().equals(itemId))
                .findFirst()
                .orElseThrow();
    }

    private static TitlePageComponent component() {
        return new TitlePageComponent(
                List.of("Pessoa Autora Teste 01"),
                "Titulo do Trabalho",
                Optional.of("Subtitulo do trabalho"),
                new TitlePageNature(
                        "Trabalho de conclusao de curso",
                        "obtencao do titulo de graduacao",
                        "Analise e Desenvolvimento de Sistemas",
                        "Universidade Paulista - UNIP"
                ),
                Optional.of(new AcademicPerson("Pessoa Orientadora Teste", Optional.of("Prof. Dr."))),
                Optional.empty(),
                "Limeira",
                "2026"
        );
    }

    private static TitlePageComponent componentWithoutAdvisor() {
        return new TitlePageComponent(
                List.of("Pessoa Autora Teste 01"),
                "Titulo do Trabalho",
                Optional.of("Subtitulo do trabalho"),
                new TitlePageNature(
                        "Trabalho de conclusao de curso",
                        "obtencao do titulo de graduacao",
                        "Analise e Desenvolvimento de Sistemas",
                        "Universidade Paulista - UNIP"
                ),
                Optional.empty(),
                Optional.empty(),
                "Limeira",
                "2026"
        );
    }

    private static TitlePageComponent componentWithCoadvisor() {
        return new TitlePageComponent(
                List.of("Pessoa Autora Teste 01"),
                "Titulo do Trabalho",
                Optional.of("Subtitulo do trabalho"),
                new TitlePageNature(
                        "Trabalho de conclusao de curso",
                        "obtencao do titulo de graduacao",
                        "Analise e Desenvolvimento de Sistemas",
                        "Universidade Paulista - UNIP"
                ),
                Optional.of(new AcademicPerson("Pessoa Orientadora Teste", Optional.of("Prof. Dr."))),
                Optional.of(new AcademicPerson("Pessoa Coorientadora Teste", Optional.of("Profa. Dra."))),
                "Limeira",
                "2026"
        );
    }
}
