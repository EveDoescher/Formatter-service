package com.abntbuilder.formatter.rendering.component.cover.layout;

import com.abntbuilder.formatter.document.component.cover.CoverComponent;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.PageOrientation;
import com.abntbuilder.formatter.profile.model.PageRule;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.StyleType;
import com.abntbuilder.formatter.profile.model.TextAlignment;
import com.abntbuilder.formatter.profile.model.component.cover.CoverComponentRule;
import com.abntbuilder.formatter.profile.model.component.cover.CoverLayoutRule;
import com.abntbuilder.formatter.profile.model.component.cover.CoverStyleMapping;
import com.abntbuilder.formatter.shared.exception.SinglePageLayoutOverflowException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CoverLayoutCalculatorTest {

    private final CoverLayoutCalculator calculator = new CoverLayoutCalculator();

    @Test
    void shouldCreateValidatedPlanThatFillsEffectiveSinglePageCapacity() {
        CoverLayoutPlan plan = calculator.calculate(validCover(), validProfile());

        assertFalse(plan.elements().isEmpty());
        assertEquals(plan.pageCapacityLines(), plan.totalLines());
        assertTrue(plan.elements().stream().anyMatch(CoverSpacerLines.class::isInstance));
        assertTrue(plan.elements().stream().anyMatch(CoverTextLines.class::isInstance));
        assertTrue(plan.exactLineHeightPt().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void shouldFailBeforeRenderingWhenCoverContentDoesNotFit() {
        List<String> authors = new ArrayList<>();

        for (int index = 0; index < 40; index++) {
            authors.add("NOME COMPLETO DO ALUNO " + index);
        }

        CoverComponent cover = new CoverComponent(
                List.of("UNIVERSIDADE PAULISTA"),
                authors,
                "TITULO DO TRABALHO",
                Optional.empty(),
                List.of("Limeira", "2026")
        );

        assertThrows(
                SinglePageLayoutOverflowException.class,
                () -> calculator.calculate(cover, validProfile())
        );
    }

    @Test
    void shouldFailWhenBottomBlockWrapsToMoreThanCityAndYearLines() {
        CoverComponent cover = new CoverComponent(
                List.of("UNIVERSIDADE PAULISTA"),
                List.of("NOME COMPLETO DO ALUNO"),
                "TITULO DO TRABALHO",
                Optional.empty(),
                List.of("Cidade Com Nome Muito Grande Que Nao Deve Caber Em Uma Linha", "2026")
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(cover, validProfile())
        );

        assertEquals("cover bottomLines must contain exactly city and year.", exception.getMessage());
    }

    private static CoverComponent validCover() {
        return new CoverComponent(
                List.of("UNIVERSIDADE PAULISTA"),
                List.of("NOME COMPLETO DO ALUNO"),
                "TITULO DO TRABALHO",
                Optional.of("Subtitulo do trabalho"),
                List.of("Limeira", "2026")
        );
    }

    private static DocumentProfile validProfile() {
        return new DocumentProfile(
                "abnt-unip-profile",
                "ABNT UNIP Profile",
                validPageRule(),
                List.of(
                        style("cover.top", true, true),
                        style("cover.author", false, true),
                        style("cover.title", true, true),
                        style("cover.subtitle", false, false),
                        style("cover.bottom", false, false)
                ),
                List.of(new CoverComponentRule(
                        "cover",
                        new CoverStyleMapping(
                                "cover.top",
                                "cover.author",
                                "cover.title",
                                "cover.subtitle",
                                "cover.bottom"
                        ),
                        new CoverLayoutRule(
                                BigDecimal.valueOf(30),
                                BigDecimal.valueOf(10),
                                BigDecimal.valueOf(60)
                        )
                ))
        );
    }

    private static PageRule validPageRule() {
        return new PageRule(
                BigDecimal.valueOf(21),
                BigDecimal.valueOf(29.7),
                BigDecimal.valueOf(3),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(3),
                PageOrientation.PORTRAIT
        );
    }

    private static StyleRule style(String id, boolean bold, boolean uppercase) {
        return new StyleRule(
                id,
                StyleType.PARAGRAPH,
                "Times New Roman",
                BigDecimal.valueOf(12),
                TextAlignment.CENTER,
                BigDecimal.valueOf(1.5),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                bold,
                false,
                uppercase
        );
    }
}
