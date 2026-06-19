package com.abntbuilder.formatter.rendering.component.titlepage;

import com.abntbuilder.formatter.document.component.titlepage.AcademicPerson;
import com.abntbuilder.formatter.document.component.titlepage.TitlePageComponent;
import com.abntbuilder.formatter.document.component.titlepage.TitlePageNature;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.config.ClasspathJsonProfileProvider;
import com.abntbuilder.formatter.rendering.layout.singlepage.HorizontalPlacementResolver;
import com.abntbuilder.formatter.rendering.layout.singlepage.MarginBasedSinglePageSafetyPolicy;
import com.abntbuilder.formatter.rendering.layout.singlepage.OrderedLayoutGapResolver;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageGapDistributor;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutEngine;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutLineMetrics;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageSpacerLines;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageTextLines;
import com.abntbuilder.formatter.rendering.layout.text.FontMetricsTextMeasurer;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TitlePageLayoutCalculatorTest {

    private final TitlePageLayoutCalculator calculator = titlePageLayoutCalculator();

    @Test
    void shouldCreateSinglePagePlanFromTitlePageProfileRule() {
        DocumentProfile profile = new ClasspathJsonProfileProvider().findById("abnt-unip-profile");
        TitlePageLayoutPlan plan = calculator.calculate(validTitlePage(), profile);

        assertFalse(plan.layoutPlan().elements().isEmpty());
        assertEquals(
                plan.layoutPlan().diagnostic().renderableArea().safeHeightTwips(),
                plan.layoutPlan().diagnostic().contentHeightTwips()
                        + plan.layoutPlan().diagnostic().allocatedGapHeightTwips()
        );
        assertEquals(
                plan.layoutPlan().totalLines(),
                plan.layoutPlan().diagnostic().contentLineCount()
                        + plan.layoutPlan().diagnostic().availableGapLines()
        );
        assertTrue(plan.layoutPlan().elements().stream().anyMatch(SinglePageSpacerLines.class::isInstance));
        assertTrue(plan.layoutPlan().elements().stream().anyMatch(SinglePageTextLines.class::isInstance));
        assertTrue(plan.layoutPlan().diagnostic().groupLineCounts().containsKey("titlePage.natureBlock"));

        SinglePageTextLines nature = plan.layoutPlan()
                .elements()
                .stream()
                .filter(SinglePageTextLines.class::isInstance)
                .map(SinglePageTextLines.class::cast)
                .filter(textLines -> textLines.itemId().equals("nature"))
                .findFirst()
                .orElseThrow();

        assertTrue(nature.measurementArea().orElseThrow().leftIndentCm().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(nature.layoutOverride().leftIndentCm().isPresent());
        assertEquals(0, nature.exactLineHeightPt().compareTo(BigDecimal.valueOf(12)));
    }

    private static TitlePageComponent validTitlePage() {
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

    private static TitlePageLayoutCalculator titlePageLayoutCalculator() {
        return new TitlePageLayoutCalculator(
                new TitlePageLayoutAssembler(
                        new FontMetricsTextMeasurer(),
                        new OrderedLayoutGapResolver(),
                        new TitlePageProfileContentValidator(),
                        new TitlePageTextTemplateResolver(),
                        new HorizontalPlacementResolver()
                ),
                new SinglePageLayoutEngine(
                        new SinglePageLayoutLineMetrics(),
                        new MarginBasedSinglePageSafetyPolicy(),
                        new SinglePageGapDistributor()
                )
        );
    }
}
